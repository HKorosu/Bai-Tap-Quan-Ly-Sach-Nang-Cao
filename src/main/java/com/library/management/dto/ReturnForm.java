package com.library.management.dto;

import java.util.ArrayList;
import java.util.List;

public class ReturnForm {
    private List<ReturnLineForm> lines = new ArrayList<>();
    public List<ReturnLineForm> getLines() { return lines; }
    public void setLines(List<ReturnLineForm> lines) { this.lines = lines; }
}
