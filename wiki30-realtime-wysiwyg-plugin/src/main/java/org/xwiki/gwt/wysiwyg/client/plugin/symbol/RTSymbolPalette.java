/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.gwt.wysiwyg.client.plugin.symbol;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HTMLTable.Cell;


/**
 * A set of symbols from which the user can choose one by clicking.
 * 
 * @version $Id: 169c2b70202888cf3dd3295a6684f69a8da95a8e $
 */
public class RTSymbolPalette extends Composite implements HasSelectionHandlers<String>, ClickHandler
{
    /**
     * The selected cell in {@link #symbolGrid}.
     */
    private RTSymbolCell selectedCell;

    /**
     * Creates a new symbol palette using the given list of symbols to fill a symbol grid with the specified number of
     * rows and columns.
     * 
     * @param symbols the list of symbols that are used to fill the symbol grid
     * @param rows the number of rows in the symbol grid
     * @param columns the number of columns in the symbol grid
     */
    public RTSymbolPalette(Object[][] symbols, int rows, int columns)
    {
        Grid symbolGrid = new Grid(columns, rows);
        symbolGrid.addStyleName("xSymbolPalette");
        symbolGrid.setBorderWidth(0);
        symbolGrid.setCellPadding(0);
        symbolGrid.setCellSpacing(1);
        symbolGrid.addClickHandler(this);

        RTSymbolCell charCell;
        int j = 0;
        int k = 0;
        for (int i = 0; i < symbols.length; i++) {
            if ((Boolean) symbols[i][RTSymbolPicker.CHAR_ENABLED]) {
                charCell = new RTSymbolCell(symbols[i][RTSymbolPicker.CHAR_UNICODE].toString());
                charCell.setTitle(symbols[i][RTSymbolPicker.CHAR_TITLE].toString());
                symbolGrid.setWidget(k, j, charCell);
                symbolGrid.getCellFormatter().setHorizontalAlignment(k, j, HasHorizontalAlignment.ALIGN_CENTER);
                j++;
                if (j == rows) {
                    j = 0;
                    k++;
                }
            }
        }

        initWidget(symbolGrid);
    }

    /**
     * {@inheritDoc}
     * 
     * @see HasSelectionHandlers#addSelectionHandler(SelectionHandler)
     */
    public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler)
    {
        return addHandler(handler, SelectionEvent.getType());
    }

    /**
     * @return the symbol grid that makes up this palette
     */
    protected Grid getSymbolGrid()
    {
        return (Grid) getWidget();
    }

    /**
     * {@inheritDoc}
     * 
     * @see ClickHandler#onClick(ClickEvent)
     */
    public void onClick(ClickEvent event)
    {
        if (event.getSource() == getSymbolGrid()) {
            Cell cell = getSymbolGrid().getCellForEvent(event);
            if (cell != null) {
                setSelectedCell((RTSymbolCell) getSymbolGrid().getWidget(cell.getRowIndex(), cell.getCellIndex()));
                SelectionEvent.fire(this, getSelectedSymbol());
            }
        }
    }

    /**
     * Selects a symbol in the symbol grid.
     * 
     * @param cell the symbol cell to be selected
     */
    private void setSelectedCell(RTSymbolCell cell)
    {
        if (selectedCell != cell) {
            if (selectedCell != null) {
                selectedCell.setSelected(false);
            }
            selectedCell = cell;
            if (selectedCell != null) {
                selectedCell.setSelected(true);
            }
        }
    }

    /**
     * @return the symbol within the grid cell that was last clicked
     */
    public String getSelectedSymbol()
    {
        return selectedCell != null ? selectedCell.getSymbol() : null;
    }

    /**
     * Selects a symbol in the symbol grid.
     * 
     * @param symbol the symbol to be selected
     */
    public void setSelectedSymbol(String symbol)
    {
        if (symbol != null) {
            for (int i = 0; i < getSymbolGrid().getRowCount(); i++) {
                for (int j = 0; j < getSymbolGrid().getColumnCount(); j++) {
                    RTSymbolCell cell = (RTSymbolCell) getSymbolGrid().getWidget(i, j);
                    if (cell.getSymbol().equals(symbol)) {
                        setSelectedCell(cell);
                        return;
                    }
                }
            }
        }
        setSelectedCell(null);
    }
}
