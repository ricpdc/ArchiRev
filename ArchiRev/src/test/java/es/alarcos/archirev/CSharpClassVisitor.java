package es.alarcos.archirev;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import es.alarcos.archirev.parser.csharp.CSharpParser.*;
import es.alarcos.archirev.parser.csharp.CSharpParserVisitor;

public class CSharpClassVisitor implements CSharpParserVisitor<List<String>> {

	@Override
	public List<String> visit(ParseTree tree) {
		// TODO Auto-generated method stub
				return null;
	}

	@Override
	public List<String> visitChildren(RuleNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCompilation_unit(Compilation_unitContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNamespace_or_type_name(Namespace_or_type_nameContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType(TypeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBase_type(Base_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSimple_type(Simple_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNumeric_type(Numeric_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIntegral_type(Integral_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFloating_point_type(Floating_point_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitClass_type(Class_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType_argument_list(Type_argument_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitArgument_list(Argument_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitArgument(ArgumentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExpression(ExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNon_assignment_expression(Non_assignment_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAssignment(AssignmentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAssignment_operator(Assignment_operatorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConditional_expression(Conditional_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNull_coalescing_expression(Null_coalescing_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConditional_or_expression(Conditional_or_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConditional_and_expression(Conditional_and_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInclusive_or_expression(Inclusive_or_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExclusive_or_expression(Exclusive_or_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAnd_expression(And_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEquality_expression(Equality_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitRelational_expression(Relational_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitShift_expression(Shift_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAdditive_expression(Additive_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMultiplicative_expression(Multiplicative_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUnary_expression(Unary_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitPrimary_expression(Primary_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLiteralExpression(LiteralExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSimpleNameExpression(SimpleNameExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitParenthesisExpressions(ParenthesisExpressionsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMemberAccessExpression(MemberAccessExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLiteralAccessExpression(LiteralAccessExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitThisReferenceExpression(ThisReferenceExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBaseAccessExpression(BaseAccessExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitObjectCreationExpression(ObjectCreationExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitTypeofExpression(TypeofExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCheckedExpression(CheckedExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUncheckedExpression(UncheckedExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitDefaultValueExpression(DefaultValueExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAnonymousMethodExpression(AnonymousMethodExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSizeofExpression(SizeofExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNameofExpression(NameofExpressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMember_access(Member_accessContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBracket_expression(Bracket_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIndexer_argument(Indexer_argumentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitPredefined_type(Predefined_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExpression_list(Expression_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitObject_or_collection_initializer(Object_or_collection_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitObject_initializer(Object_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMember_initializer_list(Member_initializer_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMember_initializer(Member_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInitializer_value(Initializer_valueContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCollection_initializer(Collection_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitElement_initializer(Element_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAnonymous_object_initializer(Anonymous_object_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMember_declarator_list(Member_declarator_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMember_declarator(Member_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUnbound_type_name(Unbound_type_nameContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitGeneric_dimension_specifier(Generic_dimension_specifierContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIsType(IsTypeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLambda_expression(Lambda_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAnonymous_function_signature(Anonymous_function_signatureContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExplicit_anonymous_function_parameter_list(Explicit_anonymous_function_parameter_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExplicit_anonymous_function_parameter(Explicit_anonymous_function_parameterContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitImplicit_anonymous_function_parameter_list(Implicit_anonymous_function_parameter_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAnonymous_function_body(Anonymous_function_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitQuery_expression(Query_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFrom_clause(From_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitQuery_body(Query_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitQuery_body_clause(Query_body_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLet_clause(Let_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitWhere_clause(Where_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCombined_join_clause(Combined_join_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitOrderby_clause(Orderby_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitOrdering(OrderingContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSelect_or_group_clause(Select_or_group_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitQuery_continuation(Query_continuationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLabeledStatement(LabeledStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitDeclarationStatement(DeclarationStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEmbeddedStatement(EmbeddedStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLabeled_Statement(Labeled_StatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEmbedded_statement(Embedded_statementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEmptyStatement(EmptyStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExpressionStatement(ExpressionStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIfStatement(IfStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSwitchStatement(SwitchStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitWhileStatement(WhileStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitDoStatement(DoStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitForStatement(ForStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitForeachStatement(ForeachStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBreakStatement(BreakStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitContinueStatement(ContinueStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitGotoStatement(GotoStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitReturnStatement(ReturnStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitThrowStatement(ThrowStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitTryStatement(TryStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCheckedStatement(CheckedStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUncheckedStatement(UncheckedStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLockStatement(LockStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUsingStatement(UsingStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitYieldStatement(YieldStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUnsafeStatement(UnsafeStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixedStatement(FixedStatementContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBlock(BlockContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLocal_variable_declaration(Local_variable_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLocal_variable_type(Local_variable_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLocal_variable_declarator(Local_variable_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLocal_variable_initializer(Local_variable_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLocal_constant_declaration(Local_constant_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIf_body(If_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSwitch_section(Switch_sectionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSwitch_label(Switch_labelContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitStatement_list(Statement_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFor_initializer(For_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFor_iterator(For_iteratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCatch_clauses(Catch_clausesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSpecific_catch_clause(Specific_catch_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitGeneral_catch_clause(General_catch_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitException_filter(Exception_filterContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFinally_clause(Finally_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitResource_acquisition(Resource_acquisitionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNamespace_declaration(Namespace_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitQualified_identifier(Qualified_identifierContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNamespace_body(Namespace_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExtern_alias_directives(Extern_alias_directivesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitExtern_alias_directive(Extern_alias_directiveContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUsing_directives(Using_directivesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUsingAliasDirective(UsingAliasDirectiveContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUsingNamespaceDirective(UsingNamespaceDirectiveContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitUsingStaticDirective(UsingStaticDirectiveContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitNamespace_member_declarations(Namespace_member_declarationsContext ctx) {
		List<String> classNames = new ArrayList<>();
 		try {
			for (Namespace_member_declarationContext namespace_member_declarationContext : ctx.namespace_member_declaration()) {
				List<String> visitNamespace_member_declarationIds = this.visitNamespace_member_declaration(namespace_member_declarationContext);
				if(visitNamespace_member_declarationIds== null || visitNamespace_member_declarationIds.isEmpty()) {
					continue;
				}
				classNames.addAll(visitNamespace_member_declarationIds);
			}
		} catch (Exception e) {
			return classNames;
		}
		return classNames;
	}

	@Override
	public List<String> visitNamespace_member_declaration(Namespace_member_declarationContext ctx) {
		return this.visitType_declaration(ctx.type_declaration());
	}

	@Override
	public List<String> visitType_declaration(Type_declarationContext ctx) {
		return this.visitClass_definition(ctx.class_definition());
	}

	@Override
	public List<String> visitQualified_alias_member(Qualified_alias_memberContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType_parameter_list(Type_parameter_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType_parameter(Type_parameterContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitClass_base(Class_baseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterface_type_list(Interface_type_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType_parameter_constraints_clauses(Type_parameter_constraints_clausesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType_parameter_constraints_clause(Type_parameter_constraints_clauseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitType_parameter_constraints(Type_parameter_constraintsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitPrimary_constraint(Primary_constraintContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSecondary_constraints(Secondary_constraintsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConstructor_constraint(Constructor_constraintContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitClass_body(Class_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitClass_member_declarations(Class_member_declarationsContext ctx) {
		List<String> classNames = new ArrayList<>();
 		for (Class_member_declarationContext class_member_declarationContext : ctx.class_member_declaration()) {
			classNames.addAll(this.visitClass_member_declaration(class_member_declarationContext));
		}
		return classNames;
	}

	@Override
	public List<String> visitClass_member_declaration(Class_member_declarationContext ctx) {
		return this.visitCommon_member_declaration(ctx.common_member_declaration());
	}

	@Override
	public List<String> visitAll_member_modifiers(All_member_modifiersContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAll_member_modifier(All_member_modifierContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitCommon_member_declaration(Common_member_declarationContext ctx) {
		return this.visitClass_definition(ctx.class_definition());
	}

	@Override
	public List<String> visitTyped_member_declaration(Typed_member_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConstant_declarators(Constant_declaratorsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConstant_declarator(Constant_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitVariable_declarators(Variable_declaratorsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitVariable_declarator(Variable_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitVariable_initializer(Variable_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitReturn_type(Return_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMember_name(Member_nameContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMethod_body(Method_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFormal_parameter_list(Formal_parameter_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixed_parameters(Fixed_parametersContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixed_parameter(Fixed_parameterContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitParameter_modifier(Parameter_modifierContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitParameter_array(Parameter_arrayContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAccessor_declarations(Accessor_declarationsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitGet_accessor_declaration(Get_accessor_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitSet_accessor_declaration(Set_accessor_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAccessor_modifier(Accessor_modifierContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAccessor_body(Accessor_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEvent_accessor_declarations(Event_accessor_declarationsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAdd_accessor_declaration(Add_accessor_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitRemove_accessor_declaration(Remove_accessor_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitOverloadable_operator(Overloadable_operatorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConversion_operator_declarator(Conversion_operator_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConstructor_initializer(Constructor_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBody(BodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitStruct_interfaces(Struct_interfacesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitStruct_body(Struct_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitStruct_member_declaration(Struct_member_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitArray_type(Array_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitRank_specifier(Rank_specifierContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitArray_initializer(Array_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitVariant_type_parameter_list(Variant_type_parameter_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitVariant_type_parameter(Variant_type_parameterContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitVariance_annotation(Variance_annotationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterface_base(Interface_baseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterface_body(Interface_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterface_member_declaration(Interface_member_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterface_accessors(Interface_accessorsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEnum_base(Enum_baseContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEnum_body(Enum_bodyContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEnum_member_declaration(Enum_member_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitGlobal_attribute_section(Global_attribute_sectionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitGlobal_attribute_target(Global_attribute_targetContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAttributes(AttributesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAttribute_section(Attribute_sectionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAttribute_target(Attribute_targetContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAttribute_list(Attribute_listContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAttribute(AttributeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitAttribute_argument(Attribute_argumentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitPointer_type(Pointer_typeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixed_pointer_declarators(Fixed_pointer_declaratorsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixed_pointer_declarator(Fixed_pointer_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixed_pointer_initializer(Fixed_pointer_initializerContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitFixed_size_buffer_declarator(Fixed_size_buffer_declaratorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLocal_variable_initializer_unsafe(Local_variable_initializer_unsafeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitRight_arrow(Right_arrowContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitRight_shift(Right_shiftContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitRight_shift_assignment(Right_shift_assignmentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitLiteral(LiteralContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitBoolean_literal(Boolean_literalContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitString_literal(String_literalContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterpolated_regular_string(Interpolated_regular_stringContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterpolated_verbatium_string(Interpolated_verbatium_stringContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterpolated_regular_string_part(Interpolated_regular_string_partContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterpolated_verbatium_string_part(Interpolated_verbatium_string_partContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterpolated_string_expression(Interpolated_string_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitKeyword(KeywordContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitClass_definition(Class_definitionContext ctx) {
		return this.visitIdentifier(ctx.identifier());
	}

	@Override
	public List<String> visitStruct_definition(Struct_definitionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitInterface_definition(Interface_definitionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEnum_definition(Enum_definitionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitDelegate_definition(Delegate_definitionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitEvent_declaration(Event_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitField_declaration(Field_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitProperty_declaration(Property_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConstant_declaration(Constant_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIndexer_declaration(Indexer_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitDestructor_definition(Destructor_definitionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitConstructor_declaration(Constructor_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMethod_declaration(Method_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMethod_member_name(Method_member_nameContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitOperator_declaration(Operator_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitArg_declaration(Arg_declarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitMethod_invocation(Method_invocationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitObject_creation_expression(Object_creation_expressionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> visitIdentifier(IdentifierContext ctx) {
		List<String> id = new ArrayList<>();
		id.add(ctx.IDENTIFIER().getText());
		return id;
	}

}
