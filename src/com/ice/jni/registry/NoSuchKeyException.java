/*
** Java native interface to the Windows Registry API.
** 
** Authored by Timothy Gerard Endres
** <mailto:time@gjt.org>  <http://www.trustice.com>
** 
** This work has been placed into the public domain.
** You may use this work in any way and for any purpose you wish.
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE. 
** 
*/

package com.ice.jni.registry;

/**
 * This exception is used to indicate that no such key exists in the registry.
 *
 * @author Timothy Gerard Endres,
 *         <a href="mailto:time@ice.com">time@ice.com</a>.
 * @version 3.1.3
 */
public class NoSuchKeyException extends RegistryException {

	public NoSuchKeyException() {
	}

	public NoSuchKeyException(String msg) {
		super(msg, Registry.ERROR_FILE_NOT_FOUND);
	}

	public NoSuchKeyException(String msg, int regErr) {
		super(msg, regErr);
	}

}