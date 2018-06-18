package es.alarcos.archirev.logic.layout;

import javax.swing.SwingConstants;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.view.mxGraph;

public class ExtendedHierarchicalLayout extends mxHierarchicalLayout {

	private static int DEFAULT_SEPARATION = 50;
	
	public ExtendedHierarchicalLayout(mxGraph graph) {
		this(graph, DEFAULT_SEPARATION);
	}
	
	public ExtendedHierarchicalLayout(mxGraph graph, int separation) {
		super(graph);

		/**
		 * Specifies if the parent should be resized after the layout so that it
		 * contains all the child cells. Default is false. @See parentBorder.
		 */
		super.resizeParent = true;

		/**
		 * Specifies if the parnent should be moved if resizeParent is enabled. Default
		 * is false. @See resizeParent.
		 */
		super.moveParent = true;

		/**
		 * The border to be added around the children if the parent is to be resized
		 * using resizeParent. Default is 0. @See resizeParent.
		 */
		super.parentBorder = 0;

		/**
		 * The spacing buffer added between cells on the same layer
		 */
		super.intraCellSpacing = separation;

		/**
		 * The spacing buffer added between cell on adjacent layers
		 */
		super.interRankCellSpacing = separation;

		/**
		 * The spacing buffer between unconnected hierarchies
		 */
		super.interHierarchySpacing = separation;

		/**
		 * The distance between each parallel edge on each ranks for long edges
		 */
		super.parallelEdgeSpacing = separation;

		/**
		 * The position of the root node(s) relative to the laid out graph in. Default
		 * is <code>SwingConstants.NORTH</code>, i.e. top-down.
		 */
		super.orientation = SwingConstants.NORTH;

		/**
		 * Specifies if the STYLE_NOEDGESTYLE flag should be set on edges that are
		 * modified by the result. Default is true.
		 */
		super.disableEdgeStyle = true;

		/**
		 * Whether or not to perform local optimisations and iterate multiple times
		 * through the algorithm
		 */
		super.fineTuning = true;

		
		/**
		 * Whether or not to navigate edges whose terminal vertices have different
		 * parents but are in the same ancestry chain
		 */
		super.traverseAncestors = false;

	}

	
	@Override
	public void execute(Object parent) {
		// Execute the CompactTreeLayout
		super.execute(parent);

	}
}
