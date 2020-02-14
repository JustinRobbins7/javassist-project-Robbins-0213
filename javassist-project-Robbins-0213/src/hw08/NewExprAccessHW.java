package hw08;

import java.io.IOException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import util.UtilMenu;

public class NewExprAccessHW extends ClassLoader {

	static String _L_ = System.lineSeparator();

	public static void main(String[] args) throws Throwable {
		String[] input;
		boolean repeat = false;
		do {
			System.out.println("===================================================================");
			System.out.println("HW08 - Please enter a class, and the number of args to display.    ");
			System.out.println("Separate these values with commas.                                 ");
			System.out.println("===================================================================");

			input = UtilMenu.getArguments();
			repeat = false;

			if (input.length != 2) {
				repeat = true;
				System.out.println("[WRN] Invalid Input!");
			}
		} while (repeat);

		String classname = "target." + input[0];
		int numfields = Integer.parseInt(input[1]);

		NewExprAccessHW s = new NewExprAccessHW(numfields);
		Class<?> c = s.findClass(classname);
		Method mainMethod = c.getDeclaredMethod("main", new Class[] { String[].class });
		mainMethod.invoke(null, new Object[] { args });
	}

	private ClassPool pool;
	private int numDisplayFields = 0;

	public NewExprAccessHW() throws NotFoundException {
		pool = new ClassPool();
		pool.insertClassPath(new ClassClassPath(new java.lang.Object().getClass()));
	}

	public NewExprAccessHW(int fieldnums) throws NotFoundException {
		pool = new ClassPool();
		pool.insertClassPath(new ClassClassPath(new java.lang.Object().getClass()));
		numDisplayFields = fieldnums;
	}

	/*
	 * Finds a specified class. The bytecode for that class can be modified.
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CtClass cc = null;
		int fieldlim = this.numDisplayFields;
		try {
			cc = pool.get(name);
			cc.instrument(new ExprEditor() {
				public void edit(NewExpr newExpr) throws CannotCompileException {

					try {
						String longName = newExpr.getConstructor().getLongName();
						if (longName.startsWith("java.")) {
							return;
						}
					} catch (NotFoundException e) {
						e.printStackTrace();
					}
					CtField[] fields = newExpr.getEnclosingClass().getDeclaredFields();
					String log = String.format("[Edited by ClassLoader] new expr: %s, " //
							+ "line: %d, signature: %s", newExpr.getEnclosingClass().getName(), //
							newExpr.getLineNumber(), newExpr.getSignature());
					System.out.println(log);

					StringBuilder block = new StringBuilder();
					block.append("{" + _L_ + " $_ = $proceed($$);" + _L_);
					for (int i = 0; i < fields.length && i < fieldlim; i++) {
						block.append("{" + _L_);
						try {
							String fieldName = fields[i].getName();
							String fieldType = fields[i].getType().getName();
							block.append("    String cName = $_.getClass().getName();" + _L_);
							block.append("    String fName = $_.getClass().getDeclaredFields()[" + i + "].getName();" + _L_);
							block.append("    String fieldFullName = cName + \".\" + fName;" + _L_);
							block.append("    " + fieldType + " fieldValue = $_." + fieldName + ";" + _L_);
							block.append("    System.out.println(\"[Instrument] \" + fieldFullName + \": \" + fieldValue"
									+ ");" + _L_);
						} catch (NotFoundException e) {
							// e.printStackTrace();
							block.append("System.out.println(\"ERROR: fieldType could not be found on index " + i
									+ "\");" + _L_);
							break;
						}

						block.append("}" + _L_);
					}

					block.append("}");
					System.out.println(block);
					newExpr.replace(block.toString());
				}
			});
			byte[] b = cc.toBytecode();
			return defineClass(name, b, 0, b.length);
		} catch (NotFoundException e) {
			throw new ClassNotFoundException();
		} catch (IOException e) {
			throw new ClassNotFoundException();
		} catch (CannotCompileException e) {
			e.printStackTrace();
			throw new ClassNotFoundException();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
