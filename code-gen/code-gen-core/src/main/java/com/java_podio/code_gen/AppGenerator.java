package com.java_podio.code_gen;

import java.text.ParseException;
import java.util.List;

import com.google.common.base.CaseFormat;
import com.java_podio.code_gen.static_classes.AppWrapper;
import com.java_podio.code_gen.static_classes.PodioCurrency;
import com.java_podio.code_gen.static_classes.PodioDate;
import com.podio.app.Application;
import com.podio.app.ApplicationField;
import com.podio.contact.Profile;
import com.podio.item.FieldValuesUpdate;
import com.podio.item.FieldValuesView;
import com.podio.item.Item;
import com.podio.item.ItemCreate;
import com.podio.item.ItemUpdate;
import com.sun.codemodel.JCase;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JVar;

/**
 * Generates a java wrapper for a given podio app.
 */
public class AppGenerator {

    private static final String FIELD_IS_OF_UNSUPPORTET_TYPE_JAVADOC = "Field is of unsupportet type and is not parsed, hence always {@code null}!";

    protected JCodeModel jCodeModel;

    /**
     * Constructor - constructing an object from an {@link Item}.
     */
    JMethod constructorFromItem;

    /**
     * Sets values from a given {@link Item}.
     */
    JMethod _setValue;

    /**
     * Constructs a {@link ItemCreate} from current instance. As
     * {@link ItemCreate} inherits from {@link ItemUpdate}, the result can be
     * used for updates as well.
     */
    private JMethod _getItemCreate;

    /**
     * Initialized after first call of {@link #_getItemCreate()}.
     */
    private JVar _itemCreateFieldValues;

    private EnumGenerator enumGenerator;

    protected JPackage jp;

    /**
     * The generated app class.
     */
    private JDefinedClass jc = null;

    private JForEach setValuesFromItemForEachField;

    private JSwitch setValuesFromItemSwitch;

    private JVar itemCreateResult;

    public AppGenerator(JCodeModel jCodeModel, JPackage jPackage) throws JClassAlreadyExistsException {
	this.jCodeModel = jCodeModel;
	this.jp = jPackage;
    }

    /**
     * Generates a wrapper class for a podio app.
     * 
     * @return
     * @throws JClassAlreadyExistsException
     * @param app
     * @return
     */
    public JDefinedClass getAppClass(Application app) throws JClassAlreadyExistsException {
	// Debug:
	// CodeGenerator.printApp(app);

	String className = JavaNames.createValidJavaTypeName(app.getConfiguration().getName(), jp.name());
	jc = jp._class(className)._extends(AppWrapper.class);

	_setValue = null;
	_setValue = _setValue();
	_getItemCreate = null;
	_getItemCreate = _getItemCreate();

	enumGenerator = new EnumGenerator(jCodeModel, jp.subPackage(CaseFormat.UPPER_CAMEL.to(
		CaseFormat.LOWER_UNDERSCORE, className)));

	JDocComment jDocComment = jc.javadoc();
	jDocComment.add("Wrapper for podio app '" + app.getConfiguration().getName() + "' (id=" + app.getId()
		+ ").\nGenerated by java-podio-code-gen.");

	jc.method(JMod.PUBLIC, Integer.class, "getAppId").body()._return(JExpr.lit(app.getId()));
	jc.method(JMod.PUBLIC, String.class, "getAppExternalId")
		.body()
		._return(
			JExpr.lit(app.getConfiguration().getExternalId() == null ? "" : app.getConfiguration()
				.getExternalId()));

	// TODO add field ids (id/externalId)?

	// Default constructor:
	jc.constructor(JMod.PUBLIC);

	// itemConstructor:
	constructorFromItem = jc.constructor(JMod.PUBLIC);
	JVar constructorFromItemParam = constructorFromItem.param(Item.class,
		CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, className) + "Item");
	constructorFromItem._throws(ParseException.class);
	constructorFromItem.body().invoke(_setValue()).arg(constructorFromItemParam);

	for (ApplicationField f : app.getFields()) {
	    String name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, f.getExternalId().toLowerCase());
	    PodioType type = PodioType.forApplicationField(f);

	    String javadoc = f.getConfiguration().getDescription();
	    javadoc = attachTypeSpecificJavadoc(type, javadoc);

	    JClass javaType = getType(type, f);
	    JMember field = CodeGenerator.addMember(jc, name, javaType, javadoc, jCodeModel,
		    com.podio.app.ApplicationFieldStatus.DELETED.equals(f.getStatus()));

	    // add setValuesFromItem part:
	    JCase jcase = setValuesFromItemSwitch._case(JExpr.lit(f.getId()));
	    jcase.body().invoke(field.getSetter())
		    .arg(createGetFieldValue(type, setValuesFromItemForEachField.var(), javaType));
	    jcase.body()._break();

	    // add getItemCreate part:
	    JExpression fieldValueUpdate = createFieldValuesUpdate(field.getGetter(), type, f);
	    if (fieldValueUpdate != null) {
		JConditional cond = _getItemCreate().body()._if(JExpr.invoke(field.getGetter()).ne(JExpr._null()));
		cond._then().add(_itemCreateFieldValues.invoke("add").arg(fieldValueUpdate));
	    }
	}

	_getItemCreate().body()._return(itemCreateResult);

	CodeGenerator.addToString(jc, jCodeModel, true);

	return jc;
    }

    /**
     * @param type
     * @param javadoc
     *            might be {@code null}
     * @return (extended) javadoc, might be {@code null}
     */
    private String attachTypeSpecificJavadoc(PodioType type, String javadoc) {
	if (type.equals(PodioType.UNDEFINED)) {
	    javadoc = javadoc == null ? FIELD_IS_OF_UNSUPPORTET_TYPE_JAVADOC : javadoc + "\n"
		    + FIELD_IS_OF_UNSUPPORTET_TYPE_JAVADOC;
	} else if (type.equals(PodioType.DURATION)) {
	    javadoc = javadoc == null ? "Duration in seconds." : javadoc + "\n" + "Duration in seconds.";
	} else if (type.equals(PodioType.PROGRESS)) {
	    javadoc = javadoc == null ? "Progress: 0=min, 100=max." : javadoc + "\n" + "Progress: 0=min, 100=max.";
	} else if (type.equals(PodioType.CONTACT)) {
	    javadoc = javadoc == null ? "For updates/create only {@code profileId} is relevant." : javadoc + "\n"
		    + "For updates/create only {@code profileId} is relevant";
	}
	return javadoc;
    }

    public JMethod _setValue() throws JClassAlreadyExistsException {
	if (_setValue != null) {
	    return _setValue;
	}

	_setValue = jc.method(JMod.PUBLIC, jCodeModel.VOID, "setValue");
	_setValue._throws(ParseException.class);
	_setValue.annotate(SuppressWarnings.class).param("value", "unchecked");
	JVar setValuesFromItemParam = _setValue.param(Item.class,
		CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, "item"));
	_setValue.body().add((JExpr._super().invoke("setValue").arg(setValuesFromItemParam)));
	setValuesFromItemForEachField = _setValue.body().forEach(jCodeModel.ref(FieldValuesView.class), "field",
		setValuesFromItemParam.invoke("getFields"));
	setValuesFromItemSwitch = setValuesFromItemForEachField.body()._switch(
		setValuesFromItemForEachField.var().invoke("getId"));
	setValuesFromItemSwitch
		._default()
		.body()
		.directStatement(
			"System.out.println(\"ERROR: unexpected field id=\"+field.getId() (App: \"+this.getClass().getName()+\"");
	setValuesFromItemSwitch._default().body()._break();

	return _setValue;
    }

    public JMethod _getItemCreate() {
	if (_getItemCreate != null) {
	    return _getItemCreate;
	}
	// getItemCreate method:
	_getItemCreate = jc.method(JMod.PUBLIC, jCodeModel._ref(ItemCreate.class), "getItemCreate");
	itemCreateResult = _getItemCreate.body().decl(jCodeModel.ref(ItemCreate.class), "result",
		JExpr._super().invoke("getItemCreate"));
	_itemCreateFieldValues = _getItemCreate.body().decl(jCodeModel.ref(List.class).narrow(FieldValuesUpdate.class),
		"fieldValuesList", itemCreateResult.invoke("getFields"));

	// as statements are added later, the return statement has to be added
	// later as well!
	// _getItemCreate.body()._return(itemCreateResult);

	return _getItemCreate;

    }

    private JClass getType(PodioType type, ApplicationField f) {
	JClass result;
	switch (type) {
	case MONEY:
	    result = jCodeModel.ref(PodioCurrency.class);
	    break;
	case CATEGORY_MULTI:
	case CATEGORY_SINGLE:
	    String name = JavaNames.createValidJavaTypeName(f.getConfiguration().getLabel(), jp.name());
	    try {
		result = enumGenerator.generateEnum(f, name);
		if (type.equals(PodioType.CATEGORY_MULTI)) {
		    result = jCodeModel.ref(List.class).narrow(result);
		}
	    } catch (JClassAlreadyExistsException e) {
		System.out.println("ERROR: could not generate enum with name: " + name + "(might exist twice?!)");
		e.printStackTrace();
		result = jCodeModel.ref(Integer.class);
	    }
	    break;
	case CONTACT:
	    result = jCodeModel.ref(List.class).narrow(Profile.class);
	    break;
	case APP:
	    result = jCodeModel.ref(List.class).narrow(Integer.class);
	    break;

	default:
	    result = jCodeModel.ref(type.getJavaType());
	    break;
	}
	return result;
    }

    /**
     * JavaType -> Item
     * 
     * @param getter
     * @param type
     *            (return-)type of {@code getter}
     * @param f
     *            corresponds to (field of) {@code getter}
     * @return an {@link JExpression} that evaluates to a
     *         {@link FieldValuesUpdate} containing the return value of
     *         {@code getter}. If the field type is not supported, returns
     *         {@code null}.
     * @throws JClassAlreadyExistsException
     */
    private JExpression createFieldValuesUpdate(JMethod getter, PodioType type, ApplicationField f)
	    throws JClassAlreadyExistsException {
	switch (type) {
	case TEXT:
	    return JExpr
		    ._new(jCodeModel.ref(FieldValuesUpdate.class))
		    .arg(f.getExternalId())
		    .arg("value")
		    .arg(JOp.cond(JExpr.invoke(getter).invoke("length").eq(JExpr.lit(0)), JExpr.lit(" "),
			    JExpr.invoke(getter)));
	case NUMBER:
	case DURATION:
	case PROGRESS:
	    return JExpr._new(jCodeModel.ref(FieldValuesUpdate.class)).arg(f.getExternalId()).arg("value")
		    .arg(JExpr.invoke(getter));
	    // all PodioField implementations are treated equally here:
	case DATE:
	case MONEY:
	    return JExpr.invoke(getter).invoke("getFieldValuesUpdate").arg(JExpr.lit(f.getExternalId()));
	case CATEGORY_SINGLE:
	    // new FieldValuesUpdate("status", "value",
	    // customer.getPowerStatus().getId())
	    return JExpr._new(jCodeModel.ref(FieldValuesUpdate.class)).arg(f.getExternalId()).arg("value")
		    .arg(JExpr.invoke(getter).invoke("getPodioId"));
	case CATEGORY_MULTI:
	    return JExpr.invoke("getFielddValuesUpdateFromMultiCategory").arg(JExpr.invoke(getter))
		    .arg(f.getExternalId());
	case CONTACT:
	    return JExpr.invoke("getFieldValuesUpdateFromContacts").arg(JExpr.invoke(getter)).arg(f.getExternalId());
	case APP:
	    return JExpr.invoke("getFieldValuesUpdateFromApp").arg(JExpr.invoke(getter)).arg(f.getExternalId());

	default:
	    return null;
	}
    }

    /**
     * Item -> JavaType
     * 
     * @param type
     * @param jVar
     *            is expected to be of type {@link FieldValuesView}
     * @param javaType
     * @return an {@link JExpression} that evaluates to the value represented in
     *         {@code jVar} and is of type {@code javaType}.
     * @throws JClassAlreadyExistsException
     */
    private JExpression createGetFieldValue(PodioType type, JVar jVar, JClass javaType)
	    throws JClassAlreadyExistsException {
	switch (type) {
	case TEXT:
	    return createGetStringFieldValue(jVar, "value", jCodeModel);
	case NUMBER:
	    return createGetDoubleFieldValue(jVar);
	case DURATION:
	case PROGRESS:
	    return createGetIntegerFieldValue(jVar);
	case MONEY:
	    return createGetCurrencyFieldValue(jVar);
	case DATE:
	    return createGetDateFieldValue(jVar);
	case CATEGORY_SINGLE:
	    JExpression podioId = JExpr.direct("(Integer) ((java.util.Map<String, ?>) " + jVar.name()
		    + ".getValues().get(0).get(\"value\")).get(\"id\")");
	    return javaType.staticInvoke("byId").arg(podioId);
	    // return JExpr.cast(
	    // Integer.class,
	    // JExpr.cast(jCodeModel.ref(Map.class).narrow(String.class,
	    // Object.class),
	    // jVar.invoke("getValues").invoke("get").arg(JExpr.lit(0)).invoke("get").arg("value"))
	    // .invoke("id"));
	case CATEGORY_MULTI:
	    return JExpr.invoke("parseMultiCategoryField").arg(jVar)
		    .arg(JExpr.dotclass(javaType.getTypeParameters().get(0)));
	case APP:
	    return JExpr.invoke("parseAppField").arg(jVar);
	case CONTACT:
	    return JExpr.invoke("parseContactField").arg(jVar);

	default:
	    System.out.println("WARNING: could not create getFieldValueExpression for type: " + type);
	    return JExpr._null();
	}
    }

    /**
     * @param jVar
     *            needs to be of type {@link FieldValuesView}.
     * @return
     */
    private JExpression createGetCurrencyFieldValue(JVar jVar) {
	return JExpr._new(jCodeModel.ref(PodioCurrency.class)).arg(createGetDoubleFieldValue(jVar))
		.arg(createGetStringFieldValue(jVar, "currency", jCodeModel));
    }

    private JExpression createGetDateFieldValue(JVar jVar) throws JClassAlreadyExistsException {
	_setValue()._throws(ParseException.class);
	constructorFromItem._throws(ParseException.class);
	JExpression start = createGetStringFieldValue(jVar, "start", jCodeModel);
	JExpression end = createGetStringFieldValue(jVar, "end", jCodeModel);
	// 2011-12-31 11:27:10
	return JExpr._new(jCodeModel.ref(PodioDate.class)).arg(start).arg(end);
    }

    /**
     * @param jVar
     *            needs to be of type {@link FieldValuesView}.
     * @return String cast of field with key {@code field}
     */
    public static JExpression createGetStringFieldValue(JVar jVar, String field, JCodeModel jCodeModel) {
	return JExpr.cast(jCodeModel._ref(String.class), jVar.invoke("getValues").invoke("get").arg(JExpr.lit(0))
		.invoke("get").arg(field));
    }

    /**
     * @param jVar
     *            needs to be of type {@link FieldValuesView}.
     * @return
     */
    private JExpression createGetDoubleFieldValue(JVar jVar) {
	return jCodeModel.ref(Double.class).staticInvoke("parseDouble")
		.arg(createGetStringFieldValue(jVar, "value", jCodeModel));
    }

    /**
     * @param jVar
     *            needs to be of type {@link FieldValuesView}.
     * @return
     */
    private JExpression createGetIntegerFieldValue(JVar jVar) {
	return JExpr.cast(jCodeModel._ref(Integer.class), jVar.invoke("getValues").invoke("get").arg(JExpr.lit(0))
		.invoke("get").arg("value"));
    }
}
