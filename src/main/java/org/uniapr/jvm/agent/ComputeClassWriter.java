/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.agent;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ComputeClassWriter extends ClassWriter { 
	 
	 private ClassLoader l = getClass().getClassLoader(); 
	  
	    public ComputeClassWriter(final int flags) { 
	        super(flags); 
	    } 
	     
	    @Override 
	    protected String getCommonSuperClass(final String type1, final String type2) { 
	        try { 
	         ClassReader info1; 
	         ClassReader info2; 
	         try { 
	          info1 = typeInfo(type1); 
	            } catch (NullPointerException e) { 
	             // May happen if class is not found 
	                throw new RuntimeException("Class not found: "+type1+": "+e.toString(), e); 
	         } 
	         try { 
	          info2 = typeInfo(type2); 
	            } catch (NullPointerException e) { 
	             // May happen if class is not found 
	                throw new RuntimeException("Class not found: "+type2+": "+e.toString(), e); 
	         } 
	 
	            if ((info1.getAccess() & Opcodes.ACC_INTERFACE) != 0) { 
	                if (typeImplements(type2, info2, type1)) { 
	                    return type1; 
	                } 
	                if ((info2.getAccess() & Opcodes.ACC_INTERFACE) != 0) { 
	                    if (typeImplements(type1, info1, type2)) { 
	                        return type2; 
	                    } 
	                } 
	                return "java/lang/Object"; 
	            } 
	            if ((info2.getAccess() & Opcodes.ACC_INTERFACE) != 0) { 
	                if (typeImplements(type1, info1, type2)) { 
	                    return type2; 
	                } else { 
	                    return "java/lang/Object"; 
	                } 
	            } 
	            StringBuilder b1 = typeAncestors(type1, info1); 
	            StringBuilder b2 = typeAncestors(type2, info2); 
	            String result = "java/lang/Object"; 
	            int end1 = b1.length(); 
	            int end2 = b2.length(); 
	            while (true) { 
	                int start1 = b1.lastIndexOf(";", end1 - 1); 
	                int start2 = b2.lastIndexOf(";", end2 - 1); 
	                if (start1 != -1 && start2 != -1 
	                        && end1 - start1 == end2 - start2) { 
	                    String p1 = b1.substring(start1 + 1, end1); 
	                    String p2 = b2.substring(start2 + 1, end2); 
	                    if (p1.equals(p2)) { 
	                        result = p1; 
	                        end1 = start1; 
	                        end2 = start2; 
	                    } else { 
	                        return result; 
	                    } 
	                } else { 
	                    return result; 
	                } 
	            } 
	        } catch (IOException e) { 
	            throw new RuntimeException(e.toString()); 
	        } catch (NullPointerException e) { 
	         // May happen if class is not found 
	            throw new RuntimeException(e.toString()); 
	        } 
	    } 
	     
	    /**
	     * Returns the internal names of the ancestor classes of the given type. 
	     * 
	     * @param type 
	     *            the internal name of a class or interface. 
	     * @param info 
	     *            the ClassReader corresponding to 'type'. 
	     * @return a StringBuilder containing the ancestor classes of 'type', 
	     *         separated by ';'. The returned string has the following format: 
	     *         ";type1;type2 ... ;typeN", where type1 is 'type', and typeN is a 
	     *         direct subclass of Object. If 'type' is Object, the returned 
	     *         string is empty. 
	     * @throws IOException 
	     *             if the bytecode of 'type' or of some of its ancestor class 
	     *             cannot be loaded. 
	     */ 
	    private StringBuilder typeAncestors(String type, ClassReader info) 
	            throws IOException { 
	        StringBuilder b = new StringBuilder(); 
	        while (!"java/lang/Object".equals(type)) { 
	            b.append(';').append(type); 
	            type = info.getSuperName(); 
	            info = typeInfo(type); 
	        } 
	        return b; 
	    } 
	 
	    /**
	     * Returns true if the given type implements the given interface. 
	     * 
	     * @param type 
	     *            the internal name of a class or interface. 
	     * @param info 
	     *            the ClassReader corresponding to 'type'. 
	     * @param itf 
	     *            the internal name of a interface. 
	     * @return true if 'type' implements directly or indirectly 'itf' 
	     * @throws IOException 
	     *             if the bytecode of 'type' or of some of its ancestor class 
	     *             cannot be loaded. 
	     */ 
	    private boolean typeImplements(String type, ClassReader info, String itf) 
	            throws IOException { 
	        while (!"java/lang/Object".equals(type)) { 
	            String[] itfs = info.getInterfaces(); 
	            for (int i = 0; i < itfs.length; ++i) { 
	                if (itfs[i].equals(itf)) { 
	                    return true; 
	                } 
	            } 
	            for (int i = 0; i < itfs.length; ++i) { 
	                if (typeImplements(itfs[i], typeInfo(itfs[i]), itf)) { 
	                    return true; 
	                } 
	            } 
	            type = info.getSuperName(); 
	            info = typeInfo(type); 
	        } 
	        return false; 
	    } 
	     
	    /**
	     * Returns a ClassReader corresponding to the given class or interface. 
	     * 
	     * @param type 
	     *            the internal name of a class or interface. 
	     * @return the ClassReader corresponding to 'type'. 
	     * @throws IOException 
	     *             if the bytecode of 'type' cannot be loaded. 
	     * @throws NullPointerException 
	     *             if the bytecode of 'type' cannot be found. 
	     */ 
	    private ClassReader typeInfo(final String type) throws IOException, NullPointerException { 
	        InputStream is = l.getResourceAsStream(type + ".class"); 
	        try { 
	         if(is == null) 
	          throw new NullPointerException("Class not found "+type); 
	            return new ClassReader(is); 
	        } finally { 
	         if(is != null) 
	          is.close(); 
	        } 
	    } 
	}