/*
 * TOZNY NON-COMMERCIAL LICENSE
 *
 * Tozny dual licenses this product. For commercial use, please contact
 * info@tozny.com. For non-commercial use, the contents of this file are
 * subject to the TOZNY NON-COMMERCIAL LICENSE (the "License") which
 * permits use of the software only by government agencies, schools,
 * universities, non-profit organizations or individuals on projects that
 * do not receive external funding other than government research grants
 * and contracts.  Any other use requires a commercial license. You may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://tozny.com/legal/non-commercial-license.
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License. Portions of the software are Copyright (c) TOZNY LLC, 2018.
 * All rights reserved.
 *
 */

package com.tozny.e3db.crypto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import android.support.v4.content.PermissionChecker;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.SparseArray;

/**
 * Determines how a key is protected when stored on the device. Fingerprint and
 * lock screen protection require Android 23+.
 */
public abstract class KeyProtection {

  public static final int DEFAULT_LOCK_SCREEN_TIMEOUT = 60;

  public enum KeyProtectionType {
    NONE,
    FINGERPRINT,
    LOCK_SCREEN,
    PASSWORD;

    private static class Ords {
      private static final SparseArray<KeyProtectionType> ordMap;
      static {
        ordMap = new SparseArray<>();
        for(KeyProtectionType i : KeyProtectionType.values()) {
          ordMap.put(i.ordinal(), i);
        }
      }
    }

    static KeyProtectionType fromOrdinal(int ordinal) {
      KeyProtectionType keyProtectionType = Ords.ordMap.get(ordinal);
      if(keyProtectionType == null)
        throw new IllegalArgumentException("ordinal not found " + ordinal);
      return keyProtectionType;
    }
  }

  public abstract String password();
  public abstract int validUntilSecondsSinceUnlock();
  public abstract KeyProtectionType protectionType();

  /**
   * Indicates if the device and SDK support the given type of key protection.
   * @param protection
   * @return
   */
  @SuppressLint("MissingPermission")
  public static boolean protectionTypeSupported(Context ctx, KeyProtectionType protection) {
    switch(protection){
      case NONE:
      case PASSWORD:
        return true;
      case LOCK_SCREEN:
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
      case FINGERPRINT:
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                   PermissionChecker.checkSelfPermission(ctx, Manifest.permission.USE_FINGERPRINT) == PermissionChecker.PERMISSION_GRANTED &&
                   FingerprintManagerCompat.from(ctx).isHardwareDetected();
      default:
        throw new IllegalStateException("Unhandled protection type: " + protection);
    }
  }

  public static KeyProtection withFingerprint() {
    return new KeyProtection() {
      @Override
      public String password() {
        throw new IllegalStateException();
      }

      @Override
      public int validUntilSecondsSinceUnlock() {
        throw new IllegalStateException();
      }

      @Override
      public KeyProtectionType protectionType() {
        return KeyProtectionType.FINGERPRINT;
      }

      @Override
      public String toString() {
        return KeyProtectionType.FINGERPRINT.toString();
      }
    };
  }

  public static KeyProtection withNone() {
    return new KeyProtection() {
      @Override
      public String password() {
        throw new IllegalStateException();
      }

      @Override
      public int validUntilSecondsSinceUnlock() {
        throw new IllegalStateException();
      }

      @Override
      public KeyProtectionType protectionType() {
        return KeyProtectionType.NONE;
      }

      @Override
      public String toString() {
        return KeyProtectionType.NONE.toString();
      }
    };
  }

  public static KeyProtection withLockScreen() {
    return withLockScreen(DEFAULT_LOCK_SCREEN_TIMEOUT);
  }

  public static KeyProtection withLockScreen(final int timeoutSeconds) {

    return new KeyProtection() {
      @Override
      public KeyProtectionType protectionType() {
        return KeyProtectionType.LOCK_SCREEN;
      }

      @Override
      public String password() {
        throw new IllegalStateException();
      }

      @Override
      public int validUntilSecondsSinceUnlock() {
        return timeoutSeconds;
      }

      @Override
      public String toString() {
        return KeyProtectionType.LOCK_SCREEN.toString();
      }
    };
  }

  public static KeyProtection withPassword() {

    return new KeyProtection() {

      @Override
      public KeyProtectionType protectionType() {
        return KeyProtectionType.PASSWORD;
      }

      @Override
      public String password() {
        throw new IllegalStateException();
      }

      @Override
      public int validUntilSecondsSinceUnlock() {
        throw new IllegalStateException();
      }

      @Override
      public String toString() {
        return "(Known) " + KeyProtectionType.PASSWORD.toString();
      }
    };
  }

  public static KeyProtection fromProtection(KeyProtectionType protection) {
    switch(protection) {
      case NONE:
        return withNone();
      case FINGERPRINT:
        return withFingerprint();
      case LOCK_SCREEN:
        return withLockScreen(DEFAULT_LOCK_SCREEN_TIMEOUT);
      case PASSWORD:
        return withPassword();
      default:
        throw new IllegalStateException("protection: Unhandled KeyProtectionType value: " + protection);
    }
  }
}
