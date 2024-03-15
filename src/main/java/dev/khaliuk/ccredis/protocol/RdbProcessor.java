package dev.khaliuk.ccredis.protocol;

import com.ning.compress.lzf.LZFDecoder;
import dev.khaliuk.ccredis.config.ApplicationProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RdbProcessor {
    private final ApplicationProperties applicationProperties;

    public RdbProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public byte[] readFirstKey() throws IOException {
        String dir = applicationProperties.getDir();
        String dbFilename = applicationProperties.getDbFilename();
        String fullFilename = String.format("%s/%s", dir, dbFilename);
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(fullFilename))) {

            // Step 1: traverse the file up to resizedb field, which is indicated by 0xFB byte
            byte b = inputStream.readByte();
            while ((b & 0xFB) != 0xFB) {
                b = inputStream.readByte();
            }

            // Step 2: read 2 length-encoded sizes - hash table and expire hash table
            readLengthEncodedInt(inputStream);
            readLengthEncodedInt(inputStream);

            // Step 3: key-value pairs
            byte[] key = readKeyValuePair(inputStream).getLeft();
            System.out.println("First key read: " + new String(key));
            return key;

        } catch (FileNotFoundException e) {
            System.out.println("RDB file is not present");
            return new byte[]{};
        }
    }

    private int readLengthEncodedInt(DataInputStream inputStream) throws IOException {
        final byte TWO_LEFTMOST_BITS = (byte) 0b1100_0000;
        byte first = inputStream.readByte();
        if ((first & TWO_LEFTMOST_BITS) == 0b0000_0000) {
            return first;
        } else if ((first & TWO_LEFTMOST_BITS) == 0b0100_0000) {
            byte second = inputStream.readByte();
            return (first & 0b0011_1111) << 8 + second & 0xFF;
        } else if ((first & 0b1000_0000) > 0) {
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result = result << 8 + inputStream.readByte() & 0xFF;
            }
            return result;
        } else if ((first & TWO_LEFTMOST_BITS) == TWO_LEFTMOST_BITS) {
            if ((first & 0b0011_1111) == 0) {
                return inputStream.readByte();
            } else if ((first & 0b0011_1111) == 1) {
                int result = 0;
                for (int i = 0; i < 2; i++) {
                    result = result << 8 + inputStream.readByte() & 0xFF;
                }
                return result;
            } else if ((first & 0b0011_1111) == 2) {
                int result = 0;
                for (int i = 0; i < 4; i++) {
                    result = result << 8 + inputStream.readByte() & 0xFF;
                }
                return result;
            }
            // skip compressed length here
        }
        throw new RuntimeException("Unexpected bits: " + first);
    }

    private byte[] readEncodedString(DataInputStream inputStream) throws IOException {
        int stringSize = 0;

        final byte TWO_LEFTMOST_BITS = (byte) 0b1100_0000;
        byte first = inputStream.readByte();
        if ((first & TWO_LEFTMOST_BITS) == 0b0000_0000) {
            stringSize = first;
        } else if ((first & TWO_LEFTMOST_BITS) == 0b0100_0000) {
            byte second = inputStream.readByte();
            stringSize = (first & 0b0011_1111) << 8 + second & 0xFF;
        } else if ((first & 0b1000_0000) > 0) {
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result = result << 8 + inputStream.readByte() & 0xFF;
            }
            stringSize = result;
        } else if ((first & TWO_LEFTMOST_BITS) == TWO_LEFTMOST_BITS) {
            if ((first & 0b0011_1111) == 0) {
                stringSize = inputStream.readByte();
            } else if ((first & 0b0011_1111) == 1) {
                int result = 0;
                for (int i = 0; i < 2; i++) {
                    result = result << 8 + inputStream.readByte() & 0xFF;
                }
                stringSize = result;
            } else if ((first & 0b0011_1111) == 2) {
                int result = 0;
                for (int i = 0; i < 4; i++) {
                    result = result << 8 + inputStream.readByte() & 0xFF;
                }
                stringSize = result;
            } else if ((first & 0b0011_1111) == 3) {
                // compressed String follows compressed length and uncompressed length
                int compressedLength = readLengthEncodedInt(inputStream);
                int uncompressedLength = readLengthEncodedInt(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream(compressedLength);
                byte[] uncompressedData = LZFDecoder.decode(buf.toByteArray());
                if (uncompressedData.length != uncompressedLength) {
                    throw new RuntimeException(String.format("Expected uncompressed length %s, but was %s",
                            uncompressedLength, uncompressedData.length));
                }
                return uncompressedData;
            }
        } else {
            throw new RuntimeException("Unexpected bits: " + first);
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(stringSize);
        for (int i = 0; i < stringSize; i++) {
            buf.write(inputStream.readByte());
        }
        return buf.toByteArray();
    }

    private Pair<byte[], byte[]> readKeyValuePair(DataInputStream inputStream) throws IOException {
        byte first = inputStream.readByte();
        if ((first & 0xFD) == 0xFD) {
            // expiry time in seconds, 4 bytes; skip for now
            for (int i = 0; i < 4; i++) inputStream.readByte();
        } else if ((first & 0xFC) == 0xFC) {
            // expiry time in ms, 8 bytes; skip for now
            for (int i = 0; i < 8; i++) inputStream.readByte();
        } else {
            // 'first' is a value type indicator, skip reading it for now
        }
        byte[] key = readEncodedString(inputStream);
        return Pair.of(key, new byte[]{});
    }
}
