package com.example.texteditor;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class PagedEditorPane extends JTextPane {
 
    private static final Color PAGE_BACKGROUND = Color.WHITE;
    
    public PagedEditorPane() {
        
        setBackground(PAGE_BACKGROUND);
        
        setEditorKit(new WrapEditorKit());
        
        
        setMargin(new Insets(20, 20, 20, 20));
    }
    
 
    private static class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory defaultFactory = new WrapColumnFactory();
        
        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
        
        private static class WrapColumnFactory implements ViewFactory {
            public View create(Element elem) {
                String kind = elem.getName();
                if (kind != null) {
                    switch (kind) {
                        case AbstractDocument.ContentElementName:
                            return new WrapLabelView(elem);
                        case AbstractDocument.ParagraphElementName:
                            return new ParagraphView(elem);
                        case AbstractDocument.SectionElementName:
                            return new BoxView(elem, View.Y_AXIS);
                        case StyleConstants.ComponentElementName:
                            return new ComponentView(elem);
                        case StyleConstants.IconElementName:
                            return new IconView(elem);
                    }
                }
                return new LabelView(elem);
            }
        }
    }
    
    
    private static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }
        
        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.X_AXIS) {
                return 0;
            }
            return super.getMinimumSpan(axis);
        }
    }

   
    public void initializeInContainer(Container container) {
      
    }
}
