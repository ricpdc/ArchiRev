package es.alarcos.archirev.shape;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class ArchiMateApplicationComponentShape extends AbstractArchiMateRectangleShape {

	@Override
	public String getCornerImagePath() {
		return null;
	}

	@Override
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state) {
		super.paintShape(canvas, state);
		Graphics2D g = canvas.getGraphics();
		int sizeComponentIcon = 5;
		Rectangle rect1 = new Rectangle((int) (state.getX() - (sizeComponentIcon*2)), (int) (state.getY() + (sizeComponentIcon)), (sizeComponentIcon*4), sizeComponentIcon);
		Rectangle rect2 = new Rectangle((int) (state.getX() - (sizeComponentIcon*2)), (int) (state.getY() + (sizeComponentIcon*3)),  (sizeComponentIcon*4), sizeComponentIcon);

		g.setColor(Color.decode("#" + (String) state.getStyle().get(mxConstants.STYLE_FILLCOLOR)));
		g.setColor(Color.decode("#" + (String) state.getStyle().get(mxConstants.STYLE_FONTCOLOR)));

		g.draw(rect1);
		g.draw(rect2);
		g.fill(rect1);
		g.fill(rect2);
		g.setFont(mxUtils.getFont(state.getStyle()));
	}

}
