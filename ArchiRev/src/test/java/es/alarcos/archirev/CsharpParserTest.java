package es.alarcos.archirev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import es.alarcos.archirev.parser.csharp.CSharpLexer;
import es.alarcos.archirev.parser.csharp.CSharpParser;
import es.alarcos.archirev.parser.csharp.CSharpParser.AttributeContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Compilation_unitContext;
import es.alarcos.archirev.parser.csharp.CSharpParser.Type_declarationContext;

@SuppressWarnings("deprecation")
class CsharpParserTest {

	private static final String C_SHARP_FILE_PATH = "C:\\Users\\Alarcos\\git\\ArchiRev\\ArchiRev\\src\\test\\resources\\csharp_sources\\test.cs";

	@Test
	void test() throws IOException {
		ANTLRFileStream input = new ANTLRFileStream(C_SHARP_FILE_PATH);
		CSharpLexer lexer = new CSharpLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CSharpParser parser = new CSharpParser(tokens);

		SyntaxErrorListener listener = new SyntaxErrorListener();
		parser.addErrorListener(listener);

//		for (SyntaxError syntaxError : listener.getSyntaxErrors()) {
//			System.out.println(syntaxError.toString());
//		}

		
		List<Object> elements = new ArrayList<>();
		Compilation_unitContext compilation_unit = parser.compilation_unit();
		for(int i=0; i<compilation_unit.getChildCount(); i++) {
			ParseTree child = compilation_unit.getChild(i);
			elements.addAll(getElements(child));
		}
		
		
		CSharpClassVisitor visitor = new CSharpClassVisitor();
		//List<String> result = visitor.visitClass_member_declarations(parser.class_member_declarations());
		List<String> result = visitor.visitNamespace_member_declarations(parser.compilation_unit().namespace_member_declarations());


		System.out.println(result);


	}
	
	
	public List<Object> getElements(ParseTree child) {
		List<Object> res = new ArrayList<Object>();
		if(child.getChildCount()==0) {
			//base case	
				
			
		}
		if(child.getChildCount()==1) {
			ParseTree nestedChild = child.getChild(0);
			if(nestedChild.getClass().equals(Type_declarationContext.class)) {
				getElements(nestedChild);
				res.add(nestedChild);
				//System.out.println("\n"+nestedChild.getText());
				Type_declarationContext type_declarationContext = ((Type_declarationContext)nestedChild);
				System.out.println("\n"+type_declarationContext.class_definition().identifier().IDENTIFIER().getText());
			}
			else if(nestedChild.getClass().equals(AttributeContext.class)) {
				getElements(nestedChild);
				res.add(nestedChild);
				//System.out.println("\n"+nestedChild.getText());
				AttributeContext attributeContext = ((AttributeContext)nestedChild);
				System.out.println("\n"+attributeContext.getText()+":"+attributeContext.attribute_argument(0).getText());
			}
			//System.out.println("\n"+nestedChild.getClass().getSimpleName() + " --> " + nestedChild.getText());
			
		}
		else {
			for(int i=0; i<child.getChildCount(); i++) {
				ParseTree nestedChild = child.getChild(i);
				res.addAll(getElements(nestedChild));
			}
		}
		return res;
	}
	

}
