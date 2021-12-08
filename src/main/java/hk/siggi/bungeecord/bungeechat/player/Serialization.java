package hk.siggi.bungeecord.bungeechat.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.siggi.reflectionbypass.ReflectionBypass;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Serialization {

	private Serialization() {
	}
	private static final Gson gson;
	private static final Gson prettyGson;

	private static final Map<Class, List<Field>> fieldCache = new HashMap<>();
	private static final ReentrantReadWriteLock fieldCacheLock = new ReentrantReadWriteLock();
	private static final Lock fieldCacheReadLock = fieldCacheLock.readLock();
	private static final Lock fieldCacheWriteLock = fieldCacheLock.writeLock();

	public static void register(GsonBuilder gb) {
		gb.registerTypeAdapter(PlayerAccount.class, simpleTypeAdapter(PlayerAccount.class));
		gb.registerTypeAdapter(PlayerAccount.ChatPrefixType.class, enumTypeAdapter(PlayerAccount.ChatPrefixType.class));
		gb.registerTypeAdapter(Mail.class, simpleTypeAdapter(Mail.class));
		gb.registerTypeAdapter(MCBan.class, simpleTypeAdapter(MCBan.class));
		gb.registerTypeAdapter(Punishment.class, simpleTypeAdapter(Punishment.class));
		gb.registerTypeAdapter(Punishment.PunishmentAction.class, enumTypeAdapter(Punishment.PunishmentAction.class));
		gb.registerTypeAdapter(PlayerTitle.class, simpleTypeAdapter(PlayerTitle.class));
	}

	public static <O> void copyFields(O source, O destination) {
		try {
			if (source==null||destination==null){throw new NullPointerException("We can't copy nulls! lol");}
			Class cls = source.getClass();
			Class cls2 = destination.getClass();
			if (!cls.equals(cls2)) {
				throw new IllegalArgumentException("Both classes must be of the same class!");
			}
			List<Field> fields = getFields(cls);
			for (Field field : fields) {
				field.set(destination, field.get(source));
			}
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Ugh", ex);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="getFields(Class) and newInstance(Class)">
	private static List<Field> getFields(Class cls) {
		List<Field> fields;
		fieldCacheReadLock.lock();
		try {
			fields = fieldCache.get(cls);
		} finally {
			fieldCacheReadLock.unlock();
		}
		if (fields != null) {
			return fields;
		}
		fieldCacheWriteLock.lock();
		try {
			fields = fieldCache.get(cls);
			if (fields != null) {
				return fields;
			}
			fields = getFields0(cls);
			for (Iterator<Field> it = fields.iterator(); it.hasNext();) {
				Field field = it.next();
				int modifiers = field.getModifiers();
				if ((modifiers & Modifier.STATIC) != 0) {
					it.remove();
					continue;
				}
				ReflectionBypass.setAccessible(field, true);
				if ((field.getModifiers() & Modifier.FINAL) != 0) {
					ReflectionBypass.setModifiers(field, field.getModifiers() & ~Modifier.FINAL);
				}
			}
			fieldCache.put(cls, fields);
			return fields;
		} catch (Exception ex) {
			throw new RuntimeException("This shouldn't happen", ex);
		} finally {
			fieldCacheWriteLock.unlock();
		}
	}

	private static List<Field> getFields0(Class cls) {
		List<Field> fields = new LinkedList<>();
		fields.addAll(Arrays.asList(cls.getDeclaredFields()));
		if (cls != Object.class) {
			fields.addAll(getFields0(cls.getSuperclass()));
		}
		return fields;
	}

	private static <O> O newInstance(Class<O> cls) {
		try {
			Constructor<O> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
		return null;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Type Adapters">
	private static <C> TypeAdapter<C> simpleTypeAdapter(Class<C> cls) {
		return new TypeAdapter<C>() {
			private final JsonParser parser = new JsonParser();

			@Override
			public C read(JsonReader reader) throws IOException {
				JsonElement ele = parser.parse(reader);
				if (!ele.isJsonObject()) {
					return null;
				}
				JsonObject obj = ele.getAsJsonObject();
				C pa = newInstance(cls);
				List<Field> fields = getFields(cls);
				for (Field field : fields) {
					if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
						continue;
					}
					String name = field.getName();
					Class<?> cl = field.getType();
					Type type = field.getGenericType();
					JsonElement fieldElement = obj.get(name);
					if (fieldElement == null || fieldElement.isJsonNull()) {
						continue;
					}
					try {
						if (cl.isPrimitive()) {
							if (cl == boolean.class) {
								field.setBoolean(pa, fieldElement.getAsBoolean());
							} else if (cl == int.class) {
								field.setInt(pa, fieldElement.getAsInt());
							} else if (cl == long.class) {
								field.setLong(pa, fieldElement.getAsLong());
							} else if (cl == float.class) {
								field.setFloat(pa, fieldElement.getAsFloat());
							} else if (cl == double.class) {
								field.setDouble(pa, fieldElement.getAsDouble());
							} else if (cl == byte.class) {
								field.setByte(pa, fieldElement.getAsByte());
							} else if (cl == short.class) {
								field.setShort(pa, fieldElement.getAsShort());
							} else if (cl == char.class) {
								field.setChar(pa, fieldElement.getAsCharacter());
							}
						} else {
							Object fromJson = gson.fromJson(fieldElement, type);
							if (Collection.class.isAssignableFrom(cl)) {
								Collection collection = (Collection) field.get(pa);
								if (collection == null) {
									field.set(pa, fromJson);
								} else {
									collection.clear();
									collection.addAll((Collection) fromJson);
								}
							} else if (Map.class.isAssignableFrom(cl)) {
								Map map = (Map) field.get(pa);
								if (map == null) {
									field.set(pa, fromJson);
								} else {
									map.clear();
									map.putAll((Map) fromJson);
								}
							} else {
								field.set(pa, fromJson);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return pa;
			}

			@Override
			public void write(JsonWriter writer, C pa) throws IOException {
				List<Field> fields = getFields(cls);
				writer.beginObject();
				for (Field field : fields) {
					if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
						continue;
					}
					String name = field.getName();
					Class<?> cl = field.getType();
					Type type = field.getGenericType();
					writer.name(name);
					try {
						if (cl.isPrimitive()) {
							if (cl == boolean.class) {
								writer.value(field.getBoolean(pa));
							} else if (cl == int.class) {
								writer.value(field.getInt(pa));
							} else if (cl == long.class) {
								writer.value(field.getLong(pa));
							} else if (cl == float.class) {
								writer.value(field.getFloat(pa));
							} else if (cl == double.class) {
								writer.value(field.getDouble(pa));
							} else if (cl == byte.class) {
								writer.value(field.getByte(pa));
							} else if (cl == short.class) {
								writer.value(field.getShort(pa));
							} else if (cl == char.class) {
								writer.value(field.getChar(pa));
							}
						} else {
							gson.toJson(field.get(pa), type, writer);
						}
					} catch (Exception e) {
						throw new RuntimeException();
					}
				}
				writer.endObject();
			}
		};
	}

	private static <C extends Enum> TypeAdapter<C> enumTypeAdapter(Class<C> cls) {
		Method valueOfMethod;
		try {
			valueOfMethod = cls.getDeclaredMethod("valueOf", String.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new TypeAdapter<C>() {

			@Override
			public C read(JsonReader reader) throws IOException {
				String nextString = reader.nextString();
				try {
					return (C) valueOfMethod.invoke(null, nextString);
				} catch (Exception e) {
				}
				return null;
			}

			@Override
			public void write(JsonWriter writer, C t) throws IOException {
				writer.value(t.name());
			}
		};
	}
	// </editor-fold>

	static {
		GsonBuilder gb = new GsonBuilder();
		register(gb);
		gson = gb.create();
		GsonBuilder gb2 = new GsonBuilder();
		register(gb2);
		gb2.setPrettyPrinting();
		prettyGson = gb2.create();
	}

	public static Gson getGson() {
		return gson;
	}

	public static Gson getPrettyGson() {
		return prettyGson;
	}
}
