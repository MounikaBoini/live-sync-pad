package com.websocket.example.dto;

public class DocumentUpdate {
    private String username;
    private String content;
    private int cursorLine;
    private int cursorColumn;

    public DocumentUpdate() {
    }

    public DocumentUpdate(String username, String content, int cursorLine, int cursorColumn) {
        this.username = username;
        this.content = content;
        this.cursorLine = cursorLine;
        this.cursorColumn = cursorColumn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCursorLine() {
        return cursorLine;
    }

    public void setCursorLine(int cursorLine) {
        this.cursorLine = cursorLine;
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public void setCursorColumn(int cursorColumn) {
        this.cursorColumn = cursorColumn;
    }
}
