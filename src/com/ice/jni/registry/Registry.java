/*
** Java native interface to the Windows Registry API.
**
** Authored by Eric Siebeneich
**
** Derived from the work of:
**
** Authored by Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
*/

package com.ice.jni.registry;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * The Registry class provides is used to load the native
 * library DLL, as well as a placeholder for the top level
 * keys, error codes, and utility methods.
 *
 * @version 3.1.4
 */
public class Registry {
	/**
	 * The following statics are the top level keys.
	 * Without these, there is no way to get "into"
	 * the registry, since the RegOpenSubkey() call
	 * requires an existing key which contains the
	 * subkey.
	 */
	public static RegistryKey HKEY_CLASSES_ROOT;
	public static RegistryKey HKEY_CURRENT_USER;
	public static RegistryKey HKEY_LOCAL_MACHINE;
	public static RegistryKey HKEY_USERS;
	public static RegistryKey HKEY_PERFORMANCE_DATA;
	public static RegistryKey HKEY_CURRENT_CONFIG;
	public static RegistryKey HKEY_DYN_DATA;

	/**
	 * These are the Registry API error codes, which can
	 * be returned via the RegistryException.
	 */
	public static final int ERROR_SUCCESS = 0;
	public static final int ERROR_FILE_NOT_FOUND = 2;
	public static final int ERROR_ACCESS_DENIED = 5;
	public static final int ERROR_INVALID_HANDLE = 6;
	public static final int ERROR_INVALID_PARAMETER = 87;
	public static final int ERROR_CALL_NOT_IMPLEMENTED = 120;
	public static final int ERROR_INSUFFICIENT_BUFFER = 122;
	public static final int ERROR_LOCK_FAILED = 167;
	public static final int ERROR_TRANSFER_TOO_LONG = 222;
	public static final int ERROR_MORE_DATA = 234;
	public static final int ERROR_NO_MORE_ITEMS = 259;
	public static final int ERROR_BADDB = 1009;
	public static final int ERROR_BADKEY = 1010;
	public static final int ERROR_CANTOPEN = 1011;
	public static final int ERROR_CANTREAD = 1012;
	public static final int ERROR_CANTWRITE = 1013;
	public static final int ERROR_REGISTRY_RECOVERED = 1014;
	public static final int ERROR_REGISTRY_CORRUPT = 1015;
	public static final int ERROR_REGISTRY_IO_FAILED = 1016;
	public static final int ERROR_NOT_REGISTRY_FILE = 1017;
	public static final int ERROR_KEY_DELETED = 1018;

	/**
	 * These are used by dumpHex().
	 */
	private static final int ROW_BYTES = 16;
	private static final int ROW_QTR1 = 3;
	private static final int ROW_HALF = 7;
	private static final int ROW_QTR2 = 11;

	/**
	 * This is the last key used by the test program ($$).
	 */
	private static String saveKey = null;
	/**
	 * This is a HashMap which maps names to the top level keys.
	 */
	private static HashMap<String, RegistryKey> topLevelKeys = null;

	/**
	 * If true, debug the fv parameters and computation.
	 */
	public boolean debugLevel;

	/**
	 * Loads the DLL needed for the native methods, creates the
	 * toplevel keys, fills the HashMap that maps various names
	 * to the toplevel keys.
	 */
	static {
		try {
			System.loadLibrary("ICE_JNIRegistry");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("ERROR You have not installed the DLL named '" + "ICE_JNIRegistry.DLL'.\n\t" + e.getMessage());
		} catch (SecurityException e) {
			System.err.println("ERROR You do not have permission to load the DLL named '" + "ICE_JNIRegistry.DLL'.\n\t" + e.getMessage());
		}

		Registry.HKEY_CLASSES_ROOT = new RegistryKey(0x80000000, "HKEY_CLASSES_ROOT");
		Registry.HKEY_CURRENT_USER = new RegistryKey(0x80000001, "HKEY_CURRENT_USER");
		Registry.HKEY_LOCAL_MACHINE = new RegistryKey(0x80000002, "HKEY_LOCAL_MACHINE");
		Registry.HKEY_USERS = new RegistryKey(0x80000003, "HKEY_USERS");
		Registry.HKEY_PERFORMANCE_DATA = new RegistryKey(0x80000004, "HKEY_PERFORMANCE_DATA");
		Registry.HKEY_CURRENT_CONFIG = new RegistryKey(0x80000005, "HKEY_CURRENT_CONFIG");
		Registry.HKEY_DYN_DATA = new RegistryKey(0x80000006, "HKEY_DYN_DATA");

		Registry.topLevelKeys = new HashMap<String, RegistryKey>(16);

		topLevelKeys.put("HKCR", Registry.HKEY_CLASSES_ROOT);
		topLevelKeys.put("HKEY_CLASSES_ROOT", Registry.HKEY_CLASSES_ROOT);

		topLevelKeys.put("HKCU", Registry.HKEY_CURRENT_USER);
		topLevelKeys.put("HKEY_CURRENT_USER", Registry.HKEY_CURRENT_USER);

		topLevelKeys.put("HKLM", Registry.HKEY_LOCAL_MACHINE);
		topLevelKeys.put("HKEY_LOCAL_MACHINE", Registry.HKEY_LOCAL_MACHINE);

		topLevelKeys.put("HKU", Registry.HKEY_USERS);
		topLevelKeys.put("HKUS", Registry.HKEY_USERS);
		topLevelKeys.put("HKEY_USERS", Registry.HKEY_USERS);

		topLevelKeys.put("HKPD", Registry.HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_PERFORMANCE_DATA", Registry.HKEY_PERFORMANCE_DATA);

		topLevelKeys.put("HKCC", Registry.HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_CURRENT_CONFIG", Registry.HKEY_PERFORMANCE_DATA);

		topLevelKeys.put("HKDD", Registry.HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_DYN_DATA", Registry.HKEY_PERFORMANCE_DATA);
	}

	/**
	 * Get a top level key by name using the top level key Hashtable.
	 *
	 * @param keyName The name of the top level key.
	 * @return The top level RegistryKey, or null if unknown keyName.
	 * @see topLevelKeys
	 */
	public static RegistryKey getTopLevelKey(String keyName) {
		return Registry.topLevelKeys.get(keyName);
	}

	/**
	 * Open a subkey of a given top level key.
	 *
	 * @param topKey  The top level key containing the subkey.
	 * @param keyName The subkey's name.
	 * @param access  The access flag for the newly opened key.
	 * @return The newly opened RegistryKey.
	 * @see RegistryKey
	 */
	public static RegistryKey openSubkey(RegistryKey topKey, String keyName, int access) {
		RegistryKey subKey = null;

		try {
			subKey = topKey.openSubKey(keyName, access);
		} catch (NoSuchKeyException ex) {
			subKey = null;
		} catch (RegistryException ex) {
			subKey = null;
		}

		return subKey;
	}

	/**
	 * Get the description of a Registry error code.
	 *
	 * @param errCode The error code from a RegistryException
	 * @return The description of the error code.
	 */
	public static String getErrorMessage(int errCode) {
		switch (errCode) {
			case ERROR_SUCCESS:
				return "success";
			case ERROR_FILE_NOT_FOUND:
				return "key or value not found";
			case ERROR_ACCESS_DENIED:
				return "access denied";
			case ERROR_INVALID_HANDLE:
				return "invalid handle";
			case ERROR_INVALID_PARAMETER:
				return "invalid parameter";
			case ERROR_CALL_NOT_IMPLEMENTED:
				return "call not implemented";
			case ERROR_INSUFFICIENT_BUFFER:
				return "insufficient buffer";
			case ERROR_LOCK_FAILED:
				return "lock failed";
			case ERROR_TRANSFER_TOO_LONG:
				return "transfer was too long";
			case ERROR_MORE_DATA:
				return "more data buffer needed";
			case ERROR_NO_MORE_ITEMS:
				return "no more items";
			case ERROR_BADDB:
				return "bad database";
			case ERROR_BADKEY:
				return "bad key";
			case ERROR_CANTOPEN:
				return "can not open";
			case ERROR_CANTREAD:
				return "can not read";
			case ERROR_CANTWRITE:
				return "can not write";
			case ERROR_REGISTRY_RECOVERED:
				return "registry recovered";
			case ERROR_REGISTRY_CORRUPT:
				return "registry corrupt";
			case ERROR_REGISTRY_IO_FAILED:
				return "registry IO failed";
			case ERROR_NOT_REGISTRY_FILE:
				return "not a registry file";
			case ERROR_KEY_DELETED:
				return "key has been deleted";
		}

		return "errCode=" + errCode;
	}

	/**
	 * Export the textual definition for a registry key to a file.
	 * The resulting file can be re-loaded via RegEdit.
	 *
	 * @param pathName The pathname of the file into which to export.
	 * @param key      The registry key definition to export.
	 * @param descend  If true, descend and export all subkeys.
	 * @throws NoSuchKeyException Thrown by openSubKey().
	 * @throws RegistryException  Any other registry API error.
	 */
	public static void exportRegistryKey(String pathName, RegistryKey key, boolean descend)
			throws IOException, NoSuchKeyException, RegistryException {
		PrintWriter out = new PrintWriter(new FileWriter(pathName));

		out.println("REGEDIT4");
		out.println("");

		key.export(out, descend);

		out.flush();
		out.close();
	}
}