package tb.cost;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import tb.cost.ScalarResponseBodyConverters.BooleanResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.ByteResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.CharacterResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.DoubleResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.FloatResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.IntegerResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.LongResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.ShortResponseBodyConverter;
import tb.cost.ScalarResponseBodyConverters.StringResponseBodyConverter;

public final class ScalarsConverterFactory extends Converter.Factory {
	  public static ScalarsConverterFactory create() {
	    return new ScalarsConverterFactory();
	  }

	  private ScalarsConverterFactory() {
	  }

	  @Override
	  public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] annotations,
	      Retrofit retrofit) {
	    if (type == String.class
	        || type == boolean.class
	        || type == Boolean.class
	        || type == byte.class
	        || type == Byte.class
	        || type == char.class
	        || type == Character.class
	        || type == double.class
	        || type == Double.class
	        || type == float.class
	        || type == Float.class
	        || type == int.class
	        || type == Integer.class
	        || type == long.class
	        || type == Long.class
	        || type == short.class
	        || type == Short.class) {
	      return ScalarRequestBodyConverter.INSTANCE;
	    }
	    return null;
	  }

	  @Override
	  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
	      Retrofit retrofit) {
	    if (type == String.class) {
	      return StringResponseBodyConverter.INSTANCE;
	    }
	    if (type == Boolean.class) {
	      return BooleanResponseBodyConverter.INSTANCE;
	    }
	    if (type == Byte.class) {
	      return ByteResponseBodyConverter.INSTANCE;
	    }
	    if (type == Character.class) {
	      return CharacterResponseBodyConverter.INSTANCE;
	    }
	    if (type == Double.class) {
	      return DoubleResponseBodyConverter.INSTANCE;
	    }
	    if (type == Float.class) {
	      return FloatResponseBodyConverter.INSTANCE;
	    }
	    if (type == Integer.class) {
	      return IntegerResponseBodyConverter.INSTANCE;
	    }
	    if (type == Long.class) {
	      return LongResponseBodyConverter.INSTANCE;
	    }
	    if (type == Short.class) {
	      return ShortResponseBodyConverter.INSTANCE;
	    }
	    return null;
	  }
	}
