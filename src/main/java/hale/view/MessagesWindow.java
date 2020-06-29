package main.java.hale.view;

import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * The sub window showing the contents of the message log
 *
 * @author Jared
 */

public class MessagesWindow extends GameSubWindow
{
    private volatile boolean cacheDirty;

    private final HTMLTextAreaModel textAreaModel;
    private final TextArea textArea;
    private final ScrollPane scrollPane;
    private StringBuilder content;

    public MessagesWindow()
    {
        this.setTitle("Messages");

        textAreaModel = new HTMLTextAreaModel();
        textArea = new TextArea(textAreaModel);
        scrollPane = new ScrollPane(textArea);
        scrollPane.setTheme("messagespane");
        scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        add(scrollPane);

        content = new StringBuilder();

        cacheDirty = true;
    }

    @Override
    protected void layout()
    {
        super.layout();

        scrollPane.setSize(getInnerWidth(), getInnerHeight());
        scrollPane.setPosition(getInnerX(), getInnerY());
    }

    /**
     * Returns a string of all the contents of the message box currently
     *
     * @return the message box contents
     */

    public String getContents()
    {
        return content.toString();
    }

    /**
     * Adds the specified message with the specified font to the messages
     * shown.  The message will be added as its own line
     *
     * @param font the font to apply to the message
     * @param text the content of the message to add
     */

    public void addMessage(String font, String text)
    {
        synchronized (content) {
            content.append("<div style=\"font-family: ").append(font).append("; \">");

            content.append(text);

            content.append("</div>");

            // Keep the content from becoming too long
            while (content.length() > 10000) {
                int index = content.indexOf("</div>");
                content.delete(0, index + 6);
            }
        }

        cacheDirty = true;
    }

    /**
     * Updates the state of this message window with any new messages
     * that have been added or any other changes.
     */

    public void updateContent()
    {
        if (!cacheDirty) return;

        cacheDirty = false;

        String contentString;
        synchronized (content) {
            contentString = content.toString();
        }

        textAreaModel.setHtml(contentString);

        scrollPane.validateLayout();
        scrollPane.setScrollPositionY(scrollPane.getMaxScrollPosY());
    }
}
