/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package main.java.hale.view;

import java.util.LinkedList;

import main.java.hale.Game;
import main.java.hale.util.JSEngine;

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.model.DefaultEditFieldModel;
import de.matthiasmann.twl.model.EditFieldModel;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A Widget allowing the user to enter JavaScript commands using the game's
 * built in interpreter.
 *
 * @author Jared Stephen
 */

public class ScriptConsole extends GameSubWindow
{
    private final LinkedList<String> previousCommands;
    private int previousCommandIndex;
    private Exception lastException;

    private final EditField editField;

    private final StringBuilder textAreaContent;
    private final HTMLTextAreaModel textAreaModel;
    private final TextArea textArea;
    private final ScrollPane scrollPane;

    private final JSEngine jsEngine;

    private int autocompleteHeight, autocompleteIndent;

    /**
     * Creates a new ScriptConsole widget
     */

    public ScriptConsole()
    {
        previousCommands = new LinkedList<String>();

        setTitle("Script Console");

        // reserve a js engine for permanent use by this widget
        jsEngine = Game.scriptEngineManager.getPermanentEngine();

        printLastException = new ScriptPrintLastException();
        printHelp = new ScriptPrintHelp();

        // set up the widgets
        editField = new ScriptEntryField(new DefaultEditFieldModel());
        editField.setTheme("entryfield");

        textAreaContent = new StringBuilder();
        textAreaModel = new HTMLTextAreaModel();
        textArea = new TextArea(textAreaModel);
        scrollPane = new ScrollPane(textArea);
        scrollPane.setTheme("scriptpane");
        scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

        add(editField);
        add(scrollPane);
    }

    public void setEditFieldText(String text)
    {
        editField.setText(text);
        editField.requestKeyboardFocus();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo)
    {
        super.applyTheme(themeInfo);

        autocompleteHeight = themeInfo.getParameter("autocompleteheight", 0);
        autocompleteIndent = themeInfo.getParameter("autocompleteindent", 0);
    }

    @Override
    protected void layout()
    {
        super.layout();

        editField.setSize(getInnerWidth(), editField.getPreferredHeight());
        editField.setPosition(getInnerX(), getInnerBottom() - editField.getHeight());

        scrollPane.setPosition(getInnerX(), getInnerY());
        scrollPane.setSize(getInnerWidth(), editField.getY() - getInnerY());
    }

    private void putScriptObjects()
    {
        jsEngine.put("runtime", Runtime.getRuntime());
        jsEngine.put("game", Game.scriptInterface);
        jsEngine.put("view", Game.mainViewer);
        jsEngine.put("printLastException", printLastException);
        jsEngine.put("help", printHelp);
    }

    private void executeScript(String text)
    {
        putScriptObjects();

        startAppend("black");
        appendString("<span style=\"font-family: blue\">");
        appendStringFormatted("[hale]$ ");
        appendString("</span>");
        appendStringFormatted(text);
        endAppend();

        previousCommands.addFirst(text);
        if (previousCommands.size() > 50) previousCommands.removeLast();
        previousCommandIndex = -1;

        Exception exception = null;
        Object returnValue = null;

        try {
            returnValue = jsEngine.eval(text);
        } catch (Exception e) {
            exception = e;
            lastException = e;
        } finally {
            if (exception == null) {
                if (returnValue != null) {
                    appendText("green", returnValue.toString());
                } else {
                    appendText("green", "Success.");
                }
            } else {
                String errorMessage = exception.getLocalizedMessage();
                int indexOfGenericPart = errorMessage.indexOf("(<Unknown source>");
                if (indexOfGenericPart != -1) errorMessage = errorMessage.substring(0, indexOfGenericPart);

                appendText("red", errorMessage);
            }
        }

        Game.mainViewer.updateInterface();
    }

    private void evaluateScriptForHelp(String script)
    {
        if (script == null || script.length() == 0) return;

        putScriptObjects();

        // try to evaluate the full script first
        Object value = null;
        try {
            value = jsEngine.eval(script);

            if (value == null) throw new Exception();

            openAutocompletePopup(script, "", value);
            return;

        } catch (Exception e) {
            // allow an error in evaluating the script to fall through
            // so we can try to evaluate the script up to the last "."
        }

        // now try to evaluate the partial script
        String nextToken;
        String eval;
        int index = script.lastIndexOf('.');
        if (index != -1) {
            eval = script.substring(0, index);
            nextToken = script.substring(index + 1, script.length());

            int endIndex = nextToken.indexOf('(');
            if (endIndex != -1) {
                nextToken = nextToken.substring(0, endIndex);
            }
        } else {
            eval = script;
            nextToken = "";
        }

        try {
            value = jsEngine.eval(eval);

            if (value == null) throw new Exception();

            openAutocompletePopup(eval, nextToken, value);

        } catch (Exception e) {
            appendText("orange", "\"" + eval + "\" not found.");
            printHelp.toString();
        }
    }

    private void openAutocompletePopup(String base, String nextToken, Object value)
    {
        ScriptAutocompletePopup popup = new ScriptAutocompletePopup(ScriptConsole.this, base, nextToken);
        popup.setContent(value);
        popup.openPopup();
        popup.setPosition(getX() + autocompleteIndent, getBottom());
        popup.setSize(getWidth() - autocompleteIndent, autocompleteHeight);
    }

    private void startAppend(String font)
    {
        textAreaContent.append("<div style=\"word-wrap: break-word; font-family: ").append(font).append("; \">");
    }

    private void endAppend()
    {
        textAreaContent.append("</div>");

        // Keep the content from becoming too long
        while (textAreaContent.length() > 50000) {
            int index = textAreaContent.indexOf("</div>");
            textAreaContent.delete(0, index + 6);
        }

        textAreaModel.setHtml(textAreaContent.toString());

        scrollPane.validateLayout();
        scrollPane.setScrollPositionY(scrollPane.getMaxScrollPosY());
    }

    private void appendString(String text)
    {
        textAreaContent.append(text);
    }

    private void appendStringFormatted(String text)
    {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    textAreaContent.append("&lt;");
                    break;
                case '>':
                    textAreaContent.append("&gt;");
                    break;
                case '&':
                    textAreaContent.append("&amp;");
                    break;
                case '"':
                    textAreaContent.append("&quot;");
                    break;
                default:
                    textAreaContent.append(ch);
            }
        }
    }

    private void appendText(String font, String text)
    {
        startAppend(font);
        appendStringFormatted(text);
        endAppend();
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);

        if (visible) editField.requestKeyboardFocus();
    }

    private class ScriptEntryField extends EditField implements EditField.Callback
    {
        // values to override the default click behavior
        private int clickCount = 0;
        private long lastClickMillis = 0l;
        private final long clickTime = 500l;

        // values to override the default selection behavior
        private int wordSelectionStart, wordSelectionEnd;

        private EditFieldModel editBuffer;

        private ScriptEntryField(EditFieldModel editBuffer)
        {
            super(null, editBuffer);

            this.editBuffer = editBuffer;

            this.addCallback(this);
        }

        @Override
        public void callback(int key)
        {
            switch (key) {
                case Event.KEY_RETURN:
                    String script = editField.getText();
                    executeScript(script);
                    editField.setText("");
                    break;
            }
        }

        @Override
        public boolean handleEvent(Event evt)
        {
            if (evt.getType() == Event.Type.MOUSE_CLICKED) {
                return false;
            }

            if (evt.getType() == Event.Type.KEY_PRESSED) {
                if (Game.mainViewer.getKeyBindings().checkToggleScriptConsole(evt.getKeyCode())) {
                    return true;
                }
            }

            boolean returnValue = super.handleEvent(evt);

            switch (evt.getType()) {
                case MOUSE_MOVED:
                    clickCount = 0;
                    break;
                case MOUSE_BTNDOWN:
                    long curTime = System.currentTimeMillis();

                    if (curTime - lastClickMillis < clickTime) {
                        clickCount++;
                    } else {
                        clickCount = 1;
                    }

                    lastClickMillis = curTime;

                    if (clickCount == 2) {
                        doubleClick(evt);
                    } else
                        if (clickCount == 3) {
                            tripleClick(evt);
                        }

                    break;
                case KEY_PRESSED:
                    switch (evt.getKeyCode()) {
                        case Event.KEY_UP:
                            previousCommandIndex++;
                            if (previousCommandIndex >= previousCommands.size()) {
                                previousCommandIndex = previousCommands.size() - 1;
                            }

                            if (previousCommandIndex != -1) {
                                editField.setText(previousCommands.get(previousCommandIndex));
                            }
                            return true;
                        case Event.KEY_DOWN:
                            previousCommandIndex--;
                            if (previousCommandIndex < -1) previousCommandIndex = -1;
                            if (previousCommandIndex != -1) {
                                editField.setText(previousCommands.get(previousCommandIndex));
                            } else {
                                editField.setText("");
                            }
                            return true;
                        case Event.KEY_ESCAPE:
                            evaluateScriptForHelp(editField.getText());
                            return true;
                        default:
                    }
                default:
            }

            return returnValue;
        }

        // override the default double click behavior to select between quotes
        @Override
        protected void selectWordFromMouse(int index)
        {
            wordSelectionStart = index;
            wordSelectionEnd = index;

            while (wordSelectionStart > 0) {
                char c = editBuffer.charAt(wordSelectionStart - 1);

                if (!checkSelectionCharacter(c)) break;

                wordSelectionStart--;
            }

            while (wordSelectionEnd < editBuffer.length()) {
                char c = editBuffer.charAt(wordSelectionEnd);

                if (!checkSelectionCharacter(c)) break;

                wordSelectionEnd++;
            }

            setSelection(wordSelectionStart, wordSelectionEnd);
        }

        /*
         * override the behavior so the mouse cursor does not flash while typing
         * (non-Javadoc)
         * @see de.matthiasmann.twl.EditField#insertChar(char)
         */

        @Override
        protected void insertChar(char ch)
        {
            super.insertChar(ch);

            resetCursorAnimation();

            // override the "enter" key behavior to do auto indenting
            if (ch == '\n') {
                boolean newBlock = false;

                int cursorPos = getCursorPos();
                int lineStart = computeLineStart(cursorPos - 1);
                int lineEnd = computeLineEnd(cursorPos - 1);

                int charPos = lineStart;
                int prevLineWhitespace = 0;
                int curLineWhitespace = 0;

                // compute the amount of indent on the previous line
                while (editBuffer.charAt(charPos) == ' ') {
                    charPos++;
                    prevLineWhitespace++;
                }

                // indent an additional 4 characters if the last line was a new block
                if (getLastNonWhitespace(lineStart, lineEnd) == '{') {
                    curLineWhitespace += 4;
                    newBlock = true;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < prevLineWhitespace + curLineWhitespace; i++) {
                    sb.append(' ');
                }

                int destPos = cursorPos + sb.length();

                // if a new block was created and there are unclosed blocks,
                // auto close the block on a new line
                if (newBlock && hasUnclosedBlocks()) {
                    sb.append('\n');
                    for (int i = 0; i < prevLineWhitespace; i++) {
                        sb.append(' ');
                    }
                    sb.append('}');
                }

                super.insertText(sb.toString());

                // move the cursor back to the correct line if we added more text
                // to end a block
                super.setCursorPos(destPos);
            }
        }

        private boolean hasUnclosedBlocks()
        {
            int unclosedBlocks = 0;

            for (int i = 0; i < editBuffer.length(); i++) {
                char c = editBuffer.charAt(i);

                switch (c) {
                    case '{':
                        unclosedBlocks++;
                        break;
                    case '}':
                        unclosedBlocks--;
                        break;
                }
            }

            return unclosedBlocks > 0;
        }

        private char getLastNonWhitespace(int start, int end)
        {
            for (int i = end; i >= start; i--) {
                char c = editBuffer.charAt(i);

                if (!Character.isWhitespace(c)) return c;
            }

            return editBuffer.charAt(start);
        }

        @Override
        protected void deletePrev()
        {
            super.deletePrev();

            resetCursorAnimation();
        }

        @Override
        protected void deleteNext()
        {
            super.deleteNext();

            resetCursorAnimation();
        }

        @Override
        protected void setCursorPos(int pos, boolean select)
        {
            super.setCursorPos(pos, select);

            resetCursorAnimation();
        }

        private void resetCursorAnimation()
        {
            getAnimationState().resetAnimationTime(StateKey.get("keyboardFocus"));
        }

        private boolean checkSelectionCharacter(char c)
        {
            if (Character.isWhitespace(c)) return false;

            if (!Character.isJavaIdentifierPart(c)) return false;

            return true;
        }

        private void doubleClick(Event evt)
        {
            // compute the selection
            selectWordFromMouse(getCursorPos());

            // setting the cursor pos will clear the selection, so set it again
            setCursorPos(wordSelectionEnd);

            setSelection(wordSelectionStart, wordSelectionEnd);
        }

        private void tripleClick(Event evt)
        {
            int lineStart = computeLineStart(getCursorPos());
            int lineEnd = computeLineEnd(getCursorPos());

            setCursorPos(lineEnd);
            setSelection(lineStart, lineEnd);
        }
    }

    private final ScriptPrintLastException printLastException;
    private final ScriptPrintHelp printHelp;

    private class ScriptPrintLastException
    {
        @Override
        public String toString()
        {

            if (lastException != null) {
                appendText("link", lastException.toString());

                for (StackTraceElement element : lastException.getStackTrace()) {
                    startAppend("red");
                    appendString("&nbsp;&nbsp;&nbsp;");
                    appendStringFormatted(element.toString());
                    endAppend();
                }
            }

            return "";
        }
    }

    private class ScriptPrintHelp
    {
        @Override
        public String toString()
        {

            appendText("purple", "Use escape to get help on the current command.");
            appendText("purple", "Top Level Objects:");
            startAppend("green");
            appendString("<p>&nbsp;&nbsp;&nbsp;help</p>");
            appendString("<p>&nbsp;&nbsp;&nbsp;printLastException</p>");
            appendString("<p>&nbsp;&nbsp;&nbsp;game</p>");
            appendString("<p>&nbsp;&nbsp;&nbsp;view</p>");
            appendString("<p>&nbsp;&nbsp;&nbsp;runtime</p>");
            endAppend();

            return "";
        }
    }
}
