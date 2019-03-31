/*
 * Copyright (C) 2011 Vex Software LLC
 * This file is part of Votifier.
 * 
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package hk.siggi.rsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * RSA utility for generating key pairs.
 * 
 * @author Sigurdur Helgason
 */
public class RSAKeyGenerator {
	/**
	 * Generates a keypair with the specified number of bits.
	 */
	public static KeyPair generate(int bits) throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits,
				RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		return keygen.generateKeyPair();
	}
	/**
	 * Generates a keypair with 2048 bits.
	 */
	public static KeyPair generate2K() throws Exception {
		return generate(2048);
	}

}
