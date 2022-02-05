package mgkim.framework.online.v1;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.logging.KLog;
import mgkim.framework.core.logging.KLoggerFactory;

@Api( tags = { KConstant.SWG_V1 } )
@RestController
public class ExamController {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	KLog klog = KLoggerFactory.getLogger(KLog.class, ExamController.class);
	
	@Autowired
	private ApplicationContext springContext;
	
	@ApiOperation(value = "(exam)")
	@RequestMapping(value = "/v1/exam", method = RequestMethod.POST)
	public @ResponseBody KOutDTO<Map> idlogin(
			@KRequestMap HashMap<String, Object> inMap,
			@RequestParam(required = true) String param1) throws Exception {
		KOutDTO<Map> outDTO = new KOutDTO<Map>();

	    Set<String> radix36Set = new HashSet<String>();
	    int a = 10;
	    while (a-- > 0) {
	      System.out.println();
	      UUID uuid1 = UUID.randomUUID();
	      ByteBuffer buffer1 = ByteBuffer.wrap(new byte[16]);
	      buffer1.putLong(uuid1.getMostSignificantBits());
	      buffer1.putLong(uuid1.getLeastSignificantBits());
	      
	      BigInteger bint = new BigInteger(1, buffer1.array());
	      UUID uuid2 = UUID.fromString(bint.toString(16).replaceFirst(
	          "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)",
	          "$1-$2-$3-$4-$5"));
	      klog.print("uuid1{15}", uuid1 + "");
	      klog.print("uuid2{15}", uuid2 + "");
	      
	      String radix10 = bint.toString(10);
	      String radix16 = bint.toString(16);
	      String radix36 = bint.toString(36);
	      klog.print("radix10{15}", radix10);
	      klog.print("radix16{15}", radix16);
	      klog.print("radix36{15}", radix36);
	      
	      radix36Set.add(radix36);
	      
	      BigInteger bint2 = new BigInteger(radix36, 36);
	      UUID uuid3 = UUID.fromString(bint2.toString(16).replaceFirst(
	          "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)",
	          "$1-$2-$3-$4-$5"));
	      klog.print("uuid3{15}", uuid3 + "");
	    }
	    
	    klog.print("size(){15}", radix36Set.size() + "");
	    klog.print("length()!=25{15}", radix36Set.stream().filter(s -> s.length() != 25).count() + "");
	    
	    Map<Integer, List<String>> result = radix36Set.stream().collect(HashMap<Integer, List<String>>::new,
	        (map, str) -> {
	          Integer key = str.length();
	          List<String> list = map.get(key);
	          if (list == null) {
	            list = new ArrayList<String>();
	            map.put(key, list);
	          }
	          list.add(str);
	        }, Map::putAll);
		return outDTO;
	}
}
