/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.internal.serialize;

import com.google.common.base.Objects;
import org.gradle.internal.Cast;
import org.gradle.internal.io.ClassLoaderObjectInputStream;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import static org.gradle.internal.serialize.BaseSerializerFactory.*;

public class WellKnownTypesSerializer<T> extends AbstractSerializer<T> {
    private static final Serializer<?>[] SERIALIZERS = new Serializer[]{
        STRING_SERIALIZER,
        BOOLEAN_SERIALIZER,
        BYTE_SERIALIZER,
        SHORT_SERIALIZER,
        INTEGER_SERIALIZER,
        LONG_SERIALIZER,
        FLOAT_SERIALIZER,
        DOUBLE_SERIALIZER,
        FILE_SERIALIZER,
        BYTE_ARRAY_SERIALIZER
    };
    private static final Class<?>[] SERIALIZER_INDEX = new Class[]{
        String.class,
        Boolean.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        File.class,
        byte[].class
    };

    private static int serializerFor(Object obj) {
        if (obj != null) {
            Class<?> clazz = obj.getClass();
            for (int i = 0; i < SERIALIZER_INDEX.length; i++) {
                if (clazz == SERIALIZER_INDEX[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Serializer<T> serializerFor(int idx) {
        if (idx == -1) {
            return null;
        }
        return Cast.uncheckedCast(SERIALIZERS[idx]);
    }

    private ClassLoader classLoader;

    public WellKnownTypesSerializer() {
        classLoader = getClass().getClassLoader();
    }

    public WellKnownTypesSerializer(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : getClass().getClassLoader();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public T read(Decoder decoder) throws Exception {
        try {
            int idx = decoder.readByte();
            if (idx == -1) {
                return (T) new ClassLoaderObjectInputStream(decoder.getInputStream(), classLoader).readObject();
            }
            return serializerFor(idx).read(decoder);
        } catch (StreamCorruptedException e) {
            return null;
        }
    }

    public void write(Encoder encoder, T value) throws IOException {
        int idx = serializerFor(value);
        encoder.writeByte((byte) idx);
        if (idx == -1) {
            ObjectOutputStream objectStr = new ObjectOutputStream(encoder.getOutputStream());
            objectStr.writeObject(value);
            objectStr.flush();
        } else {
            try {
                serializerFor(idx).write(encoder, value);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        WellKnownTypesSerializer rhs = (WellKnownTypesSerializer) obj;
        return Objects.equal(classLoader, rhs.classLoader);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), classLoader);
    }
}
