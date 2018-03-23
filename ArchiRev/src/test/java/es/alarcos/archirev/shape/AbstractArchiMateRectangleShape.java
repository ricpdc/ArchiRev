package es.alarcos.archirev.shape;

import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxRectangleShape;
import com.mxgraph.view.mxCellState;

public abstract class AbstractArchiMateRectangleShape extends mxRectangleShape {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractArchiMateRectangleShape.class);

	@Override
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state) {
		super.paintShape(canvas, state);
		Graphics2D g = canvas.getGraphics();
		Image image = null;
		
		if(getCornerImagePath() == null) {
			return;
		}
		try {
			image = ImageIO.read(new File(getCornerImagePath()));
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} finally {
			g.drawImage(image, (int) (state.getX()+state.getWidth() - 20), (int) (state.getY() + 5), 15, 15,
					canvas.getRendererPane());
		}
	}

	public abstract String getCornerImagePath();

}
