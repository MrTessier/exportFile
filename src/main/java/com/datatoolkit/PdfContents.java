/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datatoolkit;

/**
 *
 * @author Michael
 */
public class PdfContents {
    private final String _jsonString;
    private final String _fileName;
    
    public PdfContents(String jsonString, String fileName) {
        _jsonString = jsonString;
        _fileName = fileName;
    }
    
    public String getJsonString() { return _jsonString; }
    public String getFileName() { return _fileName; }
}
