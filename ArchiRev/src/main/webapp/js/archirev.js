window.onload = function() {
	resizeDiagram();
};

window.onresize = function() {
	resizeDiagram();
};

function resizeDiagram() {
	try {
		$("[id*='diagramScrollPanel']").width(0);
		$("[id*='diagramScrollPanel']").width(
				$("[id*='diagramSeparator']").width());
		
		$("[id*='kdmModelScrollPanel']").width(0);
		$("[id*='kdmModelScrollPanel']").width(
				$("[id*='kdmModelSeparator']").width());
	} catch (ex) {
		console.log(ex);
	}
}
