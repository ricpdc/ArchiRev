package es.alarcos.archirev.shape;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class ArchiMateDataObjectShape extends AbstractArchiMateRectangleShape {

	@Override
	public String getCornerImagePath() {
		return null;
	}

	@Override
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state) {
		String label = state.getLabel();
		state.setLabel("");
		super.paintShape(canvas, state);

		Graphics2D g = canvas.getGraphics();

		int offsetHead = 10;

		Rectangle rectContainer = new Rectangle((int) (state.getX()), (int) (state.getY()), (int) (state.getWidth()),
				(int) (state.getHeight()));
		Rectangle rectHead = new Rectangle((int) (state.getX()), (int) (state.getY()), (int) (state.getWidth()),
				offsetHead);
		Rectangle rectLabel = new Rectangle((int) (state.getX()), (int) (state.getY() + offsetHead),
				(int) (state.getWidth()), (int) (state.getHeight()) - offsetHead);

		g.setColor(Color.decode("#" + (String) state.getStyle().get(mxConstants.STYLE_FILLCOLOR)));

		g.fill(rectContainer);
		g.setColor(Color.decode("#" + (String) state.getStyle().get(mxConstants.STYLE_FONTCOLOR)));
		g.draw(rectContainer);
		g.draw(rectHead);
		g.draw(rectLabel);

		g.setFont(mxUtils.getFont(state.getStyle()));

		drawStringCentered(g, rectLabel, label);

	}

	private void drawStringCentered(Graphics2D g, Rectangle rect, String s) {
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(s);
		int height = metrics.getHeight();
		int ascent = metrics.getAscent();

		g.drawString(s, (float) (rect.getCenterX() - (width / 2.0)),
				(float) (rect.getCenterY() - height / 2.0) + ascent);
	}

}
