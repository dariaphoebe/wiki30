package org.xwiki.gwt.wysiwyg.client.plugin.rt;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Text;
import com.google.gwt.user.client.DOM;
import fr.loria.score.client.ClientCallback;
import fr.loria.score.client.ClientDTO;
import fr.loria.score.client.Converter;
import fr.loria.score.jupiter.model.Document;
import fr.loria.score.jupiter.model.Message;
import fr.loria.score.jupiter.tree.TreeDocument;
import fr.loria.score.jupiter.tree.operation.*;

/**
 * Callback for tree documents, wysiwyg editor
 */
public class TreeClientCallback implements ClientCallback {
    private Node nativeNode;

    public TreeClientCallback(Node nativeNode) {
        this.nativeNode = nativeNode;
    }

    @Override
    public void onConnected(ClientDTO dto, Document document, boolean updateUI) {
        if (updateUI) {
            log.finest("Updating UI for WYSIWYG. Replacing native node: " + Element.as(nativeNode).getString());
            Node newNode = Converter.fromCustomToNative(((TreeDocument) document).getRoot());
            nativeNode.getParentNode().replaceChild(newNode, nativeNode);
            nativeNode = newNode;
            log.finest("New native node: " + Element.as(nativeNode).getString());
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onExecute(Message receivedMessage) {
        log.fine("Executing received: " + receivedMessage);
        log.fine("Native node is before: " + Element.as(nativeNode).getString());
        TreeOperation operation = (TreeOperation) receivedMessage.getOperation();

        final int position = operation.getPosition();
        final int[] path = operation.getPath();
        final Node targetNode = TreeHelper.getChildNodeFromLocator(nativeNode, path);

        if (operation instanceof TreeInsertText) {
            //operates on a text node
            TreeInsertText insertText = (TreeInsertText) operation;
            String txt = String.valueOf(insertText.getText());
            Node newTextNode = com.google.gwt.dom.client.Document.get().createTextNode(txt);

            //some browsers insert a br element on an empty text area, so remove it
            Node brElement = targetNode.getChild(0);
            if (brElement != null && brElement.getNodeName().equalsIgnoreCase("br")) {
                targetNode.replaceChild(newTextNode, brElement);
            } else if (path.length == 1 && path[0] == 0) {
                targetNode.appendChild(newTextNode);
            } else {
                Text.as(targetNode).insertData(position, txt);
            }
        } else if (operation instanceof TreeDeleteText) {
            TreeDeleteText deleteText = (TreeDeleteText) operation;

            Text textNode = (Text) targetNode;
            textNode.deleteData(position, 1); //delete 1 char
        } else if (operation instanceof TreeInsertParagraph) {
            TreeInsertParagraph treeInsertParagraph = (TreeInsertParagraph) operation;
            // cases
            //1 hit enter on empty text area
            final Element p = com.google.gwt.dom.client.Document.get().createElement("p");
            if (nativeNode.getChildCount() == 0) {
                targetNode.insertFirst(p);
            } else if (nativeNode.getChildCount() == 1 && nativeNode.getFirstChild().getNodeName().equalsIgnoreCase("br")) {
                log.info("3");
                nativeNode.replaceChild(nativeNode.getFirstChild(), p);
                //2 hit enter on first line
            } else if (position == 0) {
                //2.1 enter at the start of the text
                // pull down all lines below
                p.appendChild(com.google.gwt.dom.client.Document.get().createBRElement());
                Node parentNode = targetNode.getParentElement();
                if (path.length == 1 && path[0] == 0) {
                    parentNode.insertBefore(p, targetNode);
                } else {
                    parentNode.getParentNode().insertBefore(p, parentNode);
                }
            } else {
                log.info("5");
                //split the line, assume the targetNode is text
                String actualText = targetNode.getNodeValue();
                //2.2 enter in the middle of the text
                //2.3 enter at the end of the text
                //position < actualText.length()
                Text textNode = Text.as(targetNode);
                textNode.deleteData(0, position);

                p.setInnerText(actualText.substring(0, position));

                Node parentElement = targetNode.getParentElement();
                parentElement.getParentElement().insertBefore(p, parentElement);
            }
            //3 hit enter in between , not first line
            //4 hit enter at the end , nfl

            //Get the actual text node
            //first remove from the textnode what was after caret position
        } else if (operation instanceof TreeNewParagraph) {
            TreeNewParagraph treeNewParagraph = (TreeNewParagraph) operation;
            //assume position == 0
            final Element p = com.google.gwt.dom.client.Document.get().createElement("p");
            p.appendChild(com.google.gwt.dom.client.Document.get().createBRElement());

            Node parentNode = targetNode.getParentElement();
            if (path.length == 1 && path[0] == 0) {
                parentNode.insertBefore(p, targetNode);
            } else {
                parentNode.getParentNode().insertBefore(p, parentNode);
            }
        } else if (operation instanceof TreeMergeParagraph) {
            Node p = targetNode.getParentElement();
            Node pPreviousSibling = p.getPreviousSibling();

            targetNode.removeFromParent();
            p.removeFromParent();

            Node oldTextNode = pPreviousSibling.getChild(0);
            Text newTextNode = com.google.gwt.dom.client.Document.get().createTextNode(oldTextNode.getNodeValue() + targetNode.getNodeValue());
            pPreviousSibling.replaceChild(newTextNode, oldTextNode);
        }
        log.fine("Native node is after: " + Element.as(nativeNode).getString());
    }

    private void handleNewParagraph(Node targetNode, int position) {
            String actualText = targetNode.getNodeValue();
            targetNode.setNodeValue(actualText.substring(0, position));

            //then insert new node
            Node n = targetNode.getParentElement();
            Element p = DOM.createElement("p");
            p.setInnerText(actualText.substring(position));
            n.getParentElement().insertAfter(p, n);
    }
}