package org.apache.pdfbox.examples.pdmodel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.junit.jupiter.api.Test;

/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
public class TestEmbeddedMultipleFonts {
    @Test
    void testShowTextMultipleWithValidText() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDFont font1 = new PDType1Font(FontName.HELVETICA);
            List<PDFont> fonts = new ArrayList<>();
            fonts.add(font1);

            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.beginText();
                assertDoesNotThrow(() -> EmbeddedMultipleFonts.showTextMultiple(cs, "Hello World", fonts, 12));
            }
        }
    }

    @Test
    void testShowTextMultipleWithUnsupportedCharacter() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDFont font1 = new PDType1Font(FontName.HELVETICA);
            List<PDFont> fonts = new ArrayList<>();
            fonts.add(font1);

            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                assertThrows(IllegalArgumentException.class,
                        () -> EmbeddedMultipleFonts.showTextMultiple(cs, "你好", fonts, 12));
            }
        }
    }

    @Test
    void testIsWinAnsiEncodingWithValidCharacter() {
        int unicode = 'A'; // ASCII character
        boolean result = EmbeddedMultipleFonts.isWinAnsiEncoding(unicode);
        assertDoesNotThrow(() -> {
            if (!result) {
                throw new IllegalArgumentException("Character is not WinAnsi encoded");
            }
        });
    }

    @Test
    void testIsWinAnsiEncodingWithInvalidCharacter() {
        int unicode = 0x1F600; // Emoji character
        boolean result = EmbeddedMultipleFonts.isWinAnsiEncoding(unicode);
        assertDoesNotThrow(() -> {
            if (result) {
                throw new IllegalArgumentException("Character should not be WinAnsi encoded");
            }
        });
    }

    @Test
    void testShowTextMultipleThrowsExceptionForUnsupportedCharacter() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDFont font1 = new PDType1Font(FontName.HELVETICA); // Simple font
            List<PDFont> fonts = new ArrayList<>();
            fonts.add(font1);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.beginText();
                cs.newLineAtOffset(20, 700);

                // Test with unsupported character
                String unsupportedText = "abc 😊"; // Emoji is not supported by HELVETICA
                assertThrows(IllegalArgumentException.class, () -> {
                    EmbeddedMultipleFonts.showTextMultiple(cs, unsupportedText, fonts, 20);
                });

                assertDoesNotThrow(() -> {
                    // Test with supported character
                    String supportedText = "abc";
                    EmbeddedMultipleFonts.showTextMultiple(cs, supportedText, fonts, 20);
                });

                // Test with mixed characters
                String mixedText = "abc 😊 한국 中国"
                        + " abc"; // Emoji is not supported by HELVETICA
                assertThrows(IllegalArgumentException.class, () -> {
                    EmbeddedMultipleFonts.showTextMultiple(cs, mixedText, fonts, 20);
                });

                // assert throws check message
                String expectedMessage = "U+1F60A ('.notdef') is not available in the font Helvetica, encoding: WinAnsiEncoding";
                try {
                    EmbeddedMultipleFonts.showTextMultiple(cs, mixedText, fonts, 20);
                } catch (IllegalArgumentException e) {
                    assertNotEquals(expectedMessage, e.getMessage(), "Expected friendly message");
                }

                cs.endText();
            }
        }
    }
}
