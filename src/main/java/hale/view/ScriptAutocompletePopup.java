/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2012 Jared Stephen
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

package hale.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import hale.widgets.TextAreaNoInput;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A popup window that appears whenever the user presses "ESC" while typing in the
 * script console
 *
 * @author Jared Stephen
 */

public class ScriptAutocompletePopup extends PopupWindow
{
    private ScriptConsole scriptConsole;

    private String base;
    private String nextToken;

    private Object value;

    /**
     * Creates a new PopupWindow with the specified parent widget
     *
     * @param scriptConsole the parent widget
     * @param base          the string for the base object being evaluated
     * @param nextToken     the starting letters to match against the evaluated object's members
     */

    public ScriptAutocompletePopup(ScriptConsole scriptConsole, String base, String nextToken)
    {
        super(scriptConsole);

        this.scriptConsole = scriptConsole;

        this.base = base;
        this.nextToken = nextToken;

        setCloseOnEscape(true);
        setCloseOnClickedOutside(true);
    }

    /**
     * Sets the object that this autocompletion window will display options for
     *
     * @param value the evaluated autocomplete object
     */

    public void setContent(Object value)
    {
        this.value = value;

        add(new Content());
    }

    private void appendStringFormatted(StringBuilder sb, String text)
    {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(ch);
            }
        }
    }

    private class Content extends Widget
    {
        private TextArea title;
        private ScrollPane pane;
        private DialogLayout paneContent;

        private Content()
        {
            // create the title text
            HTMLTextAreaModel titleContent = new HTMLTextAreaModel();
            StringBuilder sb = new StringBuilder();

            sb.append("<span style=\"font-family: red\">");
            appendStringFormatted(sb, base);
            sb.append("</span><span style=\"font-family: white\">");
            sb.append(" is an instance of ");
            sb.append("</span><span style=\"font-family: green\">");
            appendStringFormatted(sb, value.getClass().getName());
            sb.append("</span>");

            sb.append("<div style=\"font-family: orange\">");
            if (nextToken.length() > 0) {
                appendStringFormatted(sb, "Members starting with \"" + nextToken + "\"");
            } else {
                appendStringFormatted(sb, "Members:");
            }
            sb.append("</div>");

            titleContent.setHtml(sb.toString());

            // add the widgets
            title = new TextArea(titleContent);
            add(title);

            paneContent = new DialogLayout();

            pane = new ScrollPane(paneContent);
            pane.setFixed(ScrollPane.Fixed.HORIZONTAL);
            add(pane);

            // add the selectors for each method entry
            DialogLayout.Group mainH = paneContent.createParallelGroup();
            DialogLayout.Group mainV = paneContent.createSequentialGroup();

            // add selectors for the fields
            Field[] fields = value.getClass().getFields();
            Arrays.sort(fields, new Comparator<Field>()
            {
                @Override
                public int compare(Field f1, Field f2)
                {
                    return f1.getName().compareTo(f2.getName());
                }
            });

            for (Field field : fields) {
                String name = field.getName();

                if (!name.startsWith(nextToken)) continue;

                EntrySelector selector = new EntrySelector(field);
                mainH.addWidget(selector);
                mainV.addWidget(selector);
            }

            // add selectors for the methods
            Method[] methods = value.getClass().getMethods();
            Arrays.sort(methods, new Comparator<Method>()
            {
                @Override
                public int compare(Method m1, Method m2)
                {
                    return m1.getName().compareTo(m2.getName());
                }

            });

            for (Method method : methods) {
                String name = method.getName();

                // don't print the methods inherited from Object
                if (name.equals("wait") || name.equals("toString") || name.equals("hashCode") || name.equals("getClass") ||
                        name.equals("notify") || name.equals("notifyAll") || name.equals("equals")) {
                    continue;
                }

                if (!name.startsWith(nextToken)) continue;

                EntrySelector selector = new EntrySelector(method);
                mainH.addWidget(selector);
                mainV.addWidget(selector);
            }

            paneContent.setHorizontalGroup(mainH);
            paneContent.setVerticalGroup(mainV);
        }

        @Override
        protected void layout()
        {
            title.setSize(getInnerWidth(), title.getPreferredInnerHeight() + title.getBorderVertical());
            title.setPosition(getInnerX(), getInnerY());

            pane.setPosition(getInnerX(), title.getBottom());
            pane.setSize(getInnerWidth(), getInnerHeight() - title.getHeight());
        }
    }

    private class EntrySelector extends Button implements Runnable
    {
        private TextArea textArea;

        private String editFieldText;

        @Override
        public void run()
        {
            closePopup();
            scriptConsole.setEditFieldText(editFieldText);
        }

        @Override
        protected void layout()
        {
            textArea.setSize(getInnerWidth(), getInnerHeight());
            textArea.setPosition(getInnerX(), getInnerY());
        }

        @Override
        public int getPreferredWidth()
        {
            return textArea.getPreferredInnerWidth() + textArea.getBorderHorizontal() + getBorderHorizontal();
        }

        @Override
        public int getPreferredHeight()
        {
            return textArea.getPreferredInnerHeight() + textArea.getBorderVertical() + getBorderVertical();
        }

        private EntrySelector(Field field)
        {
            HTMLTextAreaModel model = new HTMLTextAreaModel();
            model.setHtml("<span style=\"font-family: white\">" + field.getName() + "</span>");

            textArea = new TextAreaNoInput(model);
            add(textArea);
            addCallback(this);

            this.editFieldText = base + '.' + field.getName() + '.';
        }

        private EntrySelector(Method method)
        {
            HTMLTextAreaModel model = new HTMLTextAreaModel();
            textArea = new TextAreaNoInput(model);

            java.lang.reflect.Type returnType = method.getReturnType();

            StringBuilder editFieldText = new StringBuilder();
            editFieldText.append(base);
            editFieldText.append('.');

            StringBuilder sb = new StringBuilder();
            sb.append("<span style=\"font-family: purple\">");
            if (returnType instanceof Class) {
                sb.append(((Class<?>)returnType).getSimpleName());
            } else {
                sb.append(returnType.toString());
            }
            sb.append("</span>");
            sb.append(" <span style=\"font-family: white\">");
            sb.append(method.getName());
            sb.append("(</span>");

            editFieldText.append(method.getName());
            editFieldText.append('(');

            java.lang.reflect.Type[] paramTypes = method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {

                sb.append("<span style=\"font-family: green\">");
                if (paramTypes[i] instanceof Class) {
                    sb.append(((Class<?>)paramTypes[i]).getSimpleName());
                    editFieldText.append(((Class<?>)paramTypes[i]).getSimpleName());
                } else {
                    sb.append(paramTypes[i].toString());
                    editFieldText.append(paramTypes[i].toString());
                }
                sb.append("</span>");

                if (i != paramTypes.length - 1) {
                    sb.append("<span style=\"font-family: white\">,</span>");
                    sb.append(' ');

                    editFieldText.append(", ");
                }
            }

            sb.append("<span style=\"font-family: white\">)</span>");
            editFieldText.append(')');

            model.setHtml(sb.toString());
            add(textArea);
            addCallback(this);

            this.editFieldText = editFieldText.toString();
        }
    }
}
