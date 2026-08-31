package com.ruchi.order_update_service.reader;

import com.ruchi.order_update_service.model.RawOrderRow;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class OrderCsvReader implements Iterable<RawOrderRow> {

    private final Path filePath;

    public OrderCsvReader(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Iterator<RawOrderRow> iterator() {

        try {
            BufferedReader reader = Files.newBufferedReader(filePath);

            // Skip CSV header
            reader.readLine();

            return new Iterator<>() {

                String nextLine = readNextLine();

                private String readNextLine() {
                    try {
                        return reader.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Failed to read CSV file",
                                e
                        );
                    }
                }

                @Override
                public boolean hasNext() {
                    return nextLine != null;
                }

                @Override
                public RawOrderRow next() {

                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    String currentLine = nextLine;

                    nextLine = readNextLine();

                    String[] fields = currentLine.split(",", -1);

                    if(fields.length != 4){
                        return new RawOrderRow(
                                "",
                                "",
                                "",
                                "",
                                "Expected 4 columns but found " + fields.length
                        );
                    }
                    return new RawOrderRow(
                            fields[0],
                            fields[1],
                            fields[2],
                            fields[3]
                    );
                }
            };

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to open CSV file: " + filePath,
                    e
            );
        }
    }
}