/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.crypto2.example;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.palantir.crypto2.cipher.AesCtrCipher;
import com.palantir.crypto2.cipher.SeekableCipher;
import com.palantir.crypto2.cipher.SeekableCipherFactory;
import com.palantir.crypto2.io.ByteArraySeekableInput;
import com.palantir.crypto2.io.DecryptingSeekableInput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import org.junit.Test;

public final class ExampleUsage {

    @Test
    public void decryptingSeekableInputExample() throws IOException {
        byte[] bytes = "0123456789".getBytes(StandardCharsets.UTF_8);
        SeekableCipher cipher = SeekableCipherFactory.getCipher(AesCtrCipher.ALGORITHM);
        ByteArrayOutputStream os = new ByteArrayOutputStream(bytes.length);
        Cipher encrypt = cipher.initCipher(Cipher.ENCRYPT_MODE);

        // Store this key material for future decryption
        // KeyMaterial keyMaterial = cipher.getKeyMaterial();

        // Encrypt some bytes
        CipherOutputStream encryptedStream = new CipherOutputStream(os, encrypt);
        encryptedStream.write(bytes);
        encryptedStream.close();
        byte[] encryptedBytes = os.toByteArray();

        // Bytes written to stream are encrypted
        assertThat(encryptedBytes, is(not(bytes)));

        ByteArraySeekableInput is = new ByteArraySeekableInput(encryptedBytes);
        DecryptingSeekableInput decryptedStream = new DecryptingSeekableInput(is, cipher);

        // Seek to the last byte in the decrypted stream and verify its decrypted value
        byte[] readBytes = new byte[bytes.length];
        decryptedStream.seek(bytes.length - 1);
        decryptedStream.read(readBytes, 0, 1);
        assertThat(readBytes[0], is(bytes[bytes.length - 1]));

        // Seek to the beginning of the decrypted stream and verify it's equal to the raw bytes
        decryptedStream.seek(0);
        decryptedStream.read(readBytes, 0, bytes.length);
        assertThat(readBytes, is(bytes));
    }

}
