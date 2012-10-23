package edu.uml.cs.isense.supplements;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;

/**
 * Implements the SharedPreferences class and enables the encryption of data to
 * the SharedPreferences of an application.
 * 
 * @author User "emmby" of stackoverflow.com
 * 
 */
@SuppressLint("NewApi")
public class ObscuredSharedPreferences implements SharedPreferences {
	protected static final String UTF8 = "utf-8";
	private static final char[] SEKRIT = { 'A', 'n', 'd', 'r', 'o', 'i', 'd', '9' };

	protected SharedPreferences delegate;
	protected Context context;

	/**
	 * Default constructor of the ObscuredSharedPreferences class.
	 * 
	 * @param context Context of activity to utilize these ObscuredSharedPreferences
	 * @param delegate SharedPreferences object to use
	 */
	public ObscuredSharedPreferences(Context context, SharedPreferences delegate) {
		this.delegate = delegate;
		this.context = context;
	}

	/**
	 * Class that allows the editing of the ObscuredSharedPreferences object
	 * 
	 */
	public class Editor implements SharedPreferences.Editor {
		protected SharedPreferences.Editor delegate;

		/**
		 * Default constructor of the Editor class.
		 * 
		 */
		public Editor() {
			this.delegate = ObscuredSharedPreferences.this.delegate.edit();
		}

		public Editor putBoolean(String key, boolean value) {
			delegate.putString(key, encrypt(Boolean.toString(value)));
			return this;
		}

		public Editor putFloat(String key, float value) {
			delegate.putString(key, encrypt(Float.toString(value)));
			return this;
		}

		public Editor putInt(String key, int value) {
			delegate.putString(key, encrypt(Integer.toString(value)));
			return this;
		}

		public Editor putLong(String key, long value) {
			delegate.putString(key, encrypt(Long.toString(value)));
			return this;
		}

		public Editor putString(String key, String value) {
			delegate.putString(key, encrypt(value));
			return this;
		}

		public Editor clear() {
			delegate.clear();
			return this;
		}

		public boolean commit() {
			return delegate.commit();
		}

		public Editor remove(String s) {
			delegate.remove(s);
			return this;
		}

		@SuppressLint("NewApi")
		public void apply() {
			delegate.apply();
		}

		public android.content.SharedPreferences.Editor putStringSet(
				String arg0, Set<String> arg1) {
			return null;
		}
	}

	public Editor edit() {
		return new Editor();
	}

	public Map<String, ?> getAll() {
		throw new UnsupportedOperationException(); 
	}

	public boolean getBoolean(String key, boolean defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Boolean.parseBoolean(decrypt(v)) : defValue;
	}

	public float getFloat(String key, float defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Float.parseFloat(decrypt(v)) : defValue;
	}

	public int getInt(String key, int defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Integer.parseInt(decrypt(v)) : defValue;
	}

	public long getLong(String key, long defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Long.parseLong(decrypt(v)) : defValue;
	}

	public String getString(String key, String defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? decrypt(v) : defValue;
	}

	public boolean contains(String s) {
		return delegate.contains(s);
	}

	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	@SuppressWarnings("deprecation")
	protected String encrypt(String value) {

		try {
			final byte[] bytes = value != null ? value.getBytes(UTF8)
					: new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(
					Cipher.ENCRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(
							context.getContentResolver(),
							Settings.System.ANDROID_ID).getBytes(UTF8), 20));
			return new String(Base64.encode(pbeCipher.doFinal(bytes),
					Base64.NO_WRAP), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("deprecation")
	protected String decrypt(String value) {
		try {
			final byte[] bytes = value != null ? Base64.decode(value,
					Base64.DEFAULT) : new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(
					Cipher.DECRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(
							context.getContentResolver(),
							Settings.System.ANDROID_ID).getBytes(UTF8), 20));
			return new String(pbeCipher.doFinal(bytes), UTF8);

		} catch (Exception e) {
			return "";//throw new RuntimeException(e);
		}
	}

	public Set<String> getStringSet(String arg0, Set<String> arg1) {
		return null;
	}

}
