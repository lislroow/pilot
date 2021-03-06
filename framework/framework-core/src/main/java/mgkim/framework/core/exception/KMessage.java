package mgkim.framework.core.exception;

import java.text.MessageFormat;

import org.slf4j.LoggerFactory;

public enum KMessage {

	/**E0000("정상처리되었습니다.")*/
	E0000("정상처리되었습니다."),

	/**E1001("`api처리로그` {0} 건이 이관 되었습니다."),*/
	E1001("`api처리로그` {0} 건이 이관 되었습니다."),


	/**E3001("`{0}`의 입력값이 지정되지 않아 기본값 `{1}`으로 처리합니다."),*/
	E3001("`{0}`의 입력값이 지정되지 않아 기본값 `{1}`으로 처리합니다."),



	/**E5001("api 구현체가 필수로 등록되어야 합니다. api={0}"),*/
	E5001("api 구현체가 필수로 등록되어야 합니다. api={0}"),
	/**E5002("api 구현체가 등록되지 않았습니다. api={0}"),*/
	E5002("api 구현체가 등록되지 않았습니다. api={0}"),
	/**E5003("api 구현체가 등록되지 않아 스케줄러가 비활성화 되었습니다. 스케줄러={0} api={1}"),*/
	E5003("api 구현체가 등록되지 않아 스케줄러가 비활성화 되었습니다. 스케줄러={0} api={1}"),
	/**E5004("스케줄러가 비활성화 상태입니다. 스케줄러={0}"),*/
	E5004("스케줄러가 비활성화 상태입니다. 스케줄러={0}"),
	/**E5005("site 구분이 설정되지 않았습니다."),*/
	E5005("site 구분이 설정되지 않았습니다."),
	/**E5006("`{0}`의 설정값이 지정되지 않아 기본값 `{1}`으로 설정합니다."),*/
	E5006("`{0}`의 설정값이 지정되지 않아 기본값 `{1}`으로 설정합니다."),
	/**E5007("`{0}`의 설정값이 `{1}` > `{2}` 로 설정되었습니다."),*/
	E5007("`{0}`의 설정값이 `{1}` > `{2}` 로 설정되었습니다."),
	/**E5008("`{0}`의 설정값 `{1}`이 등록되지 않았습니다."),*/
	E5008("`{0}`의 설정값 `{1}`이 등록되지 않았습니다."),
	/**E5009("`{0}` 는 관리대상이 아닙니다."),*/
	E5009("`{0}` 는 관리대상이 아닙니다."),
	/**E5010("`{0}` 는 관리대상 입니다."),*/
	E5010("`{0}` 는 관리대상 입니다."),
	/**E5011("`{0}` 스케줄러가 시작되었습니다. interval=`{1}` ms"),*/
	E5011("`{0}` 스케줄러가 시작되었습니다. interval=`{1}` ms"),
	/**E5012("`{0}` 스케줄러가 중지 되었습니다."),*/
	E5012("`{0}` 스케줄러가 중지 되었습니다."),
	/**E5013("관리 대상 스케줄러 전체가 비활성화 상태입니다. managed-scheulder-list={0}"),*/
	E5013("관리 대상 스케줄러 전체가 비활성화 상태입니다. managed-scheulder-list={0}"),
	/**E5014("`{0}` 스케줄러가 비활성화 상태이기에 시작되지 않았습니다."),*/
	E5014("`{0}` 스케줄러가 비활성화 상태이기에 시작되지 않았습니다."),
	/**E5015("`{0}` 스케줄러가 이미 실행 중입니다."),*/
	E5015("`{0}` 스케줄러가 이미 실행 중입니다."),



	/**E5101("mapper 설정 파일을 찾을 수 없습니다. 설정파일=`{0}`"),*/
	E5101("mapper 설정 파일을 찾을 수 없습니다. 설정파일=`{0}`"),
	/**E5102("typealias 가 true 일 경우 classpath에 같은 클래스명이 있으면 오류가 발생합니다."),*/
	E5102("typealias 가 true 일 경우 classpath에 같은 클래스명이 있으면 오류가 발생합니다."),

	/**E5201("spring-security `uri-authority` 매핑 정보를 초기화하는데 실패했습니다."),*/
	E5201("spring-security `uri-authority` 매핑 정보를 초기화하는데 실패했습니다."),
	/**E5202("spring-security `uri-authority` 매핑 정보가 없습니다."),*/
	E5202("spring-security `uri-authority` 매핑 정보가 없습니다."),

	/**E5301("aes 암호화키 사이즈는 128, 192, 256 bit 만 가능합니다. wrong-value = `{0}`"),*/
	E5301("aes 암호화키 사이즈는 128, 192, 256 bit 만 가능합니다. wrong-value = `{0}`"),

	/**E6001("`orgapi` 타입은 apikey 인증 방식만 허용합니다."),*/
	E6001("`orgapi` 타입은 apikey 인증 방식만 허용합니다."),
	/**E6002("filter 체크를 위한 구현체가 등록되지 않아 서비스가 제한되었습니다."),*/
	E6002("filter 체크를 위한 구현체가 등록되지 않아 서비스가 제한되었습니다."),
	/**E6003("발행된 apikey가 없습니다."),*/
	E6003("발행된 apikey가 없습니다."),
	/**E6004("token 사용자가 승인되지 않은 상태입니다."),*/
	E6004("token 사용자가 승인되지 않은 상태입니다."),
	/**E6005("token 발행 시 등록된 IP가 아닙니다."),*/
	E6005("token 발행 시 등록된 IP가 아닙니다."),
	/**E6006("발행 token 이 폐기된 상태입니다."),*/
	E6006("발행 token 이 폐기된 상태입니다."),
	/**E6007("apikey `{0}`를 Date 객체로 parsing 하는 중 오류가 발생했습니다."),*/
	E6007("apikey `{0}`를 Date 객체로 parsing 하는 중 오류가 발생했습니다."),
	/**E6008("발행 token 이 유효기간 상태가 아닙니다. (유효기간: {0} ~ {1})"),*/
	E6008("발행 token 이 유효기간 상태가 아닙니다. (유효기간: {0} ~ {1})"),
	/**E6009("accessToken 이 없습니다."),*/
	E6009("accessToken 이 없습니다."),
	/**E6010("`{0}`의 accessToken 형식이 유효하지 않습니다."),*/
	E6010("`{0}`의 accessToken 형식이 유효하지 않습니다."),
	/**E6011("api 접근 권한이 없습니다."),*/
	E6011("api 접근 권한이 없습니다."),
	/**E6012("accessToken이 만료됐습니다."),*/
	E6012("accessToken이 만료됐습니다."),
	/**E6013("`{0}`이 유효하지 않습니다."),*/
	E6013("`{0}`이 유효하지 않습니다."),
	/**E6014("`openapi` 타입은 bearer 인증 방식만 허용합니다."),*/
	E6014("`openapi` 타입은 bearer 인증 방식만 허용합니다."),
	/**E6015("`{0}` encoding 을 하는 중 오류가 발생했습니다."),*/
	E6015("`{0}` encoding 을 하는 중 오류가 발생했습니다."),
	/**E6016("JWT 토큰을 parsing 하는 중 오류가 발생했습니다."),*/
	E6016("JWT 토큰을 parsing 하는 중 오류가 발생했습니다."),
	/**E6017("JWT 타입 `{0}` 이 아닙니다."),*/
	E6017("JWT 타입 `{0}` 이 아닙니다."),
	/**E6018("`{0}` 이 만료됐습니다."),*/
	E6018("`{0}` 이 만료됐습니다."),
	/**E6019("token.guid 와 header.guid 가 일치하지 않습니다. `token.guid` = {0}, `header.guid` = {1}"),*/
	E6019("token.guid 와 header.guid 가 일치하지 않습니다. `token.guid` = {0}, `header.guid` = {1}"),
	/**E6020("`{0}` 사이트에서 생성된 `{1}` 이 아닙니다."),*/
	E6020("`{0}` 사이트에서 생성된 `{1}` 이 아닙니다."),
	/**E6021("(accessToken에 포함될) refreshToken 의 claims 를 검증하는 과정에서 변조된 것을 감지했습니다."),*/
	E6021("(accessToken에 포함될) refreshToken 의 claims 를 검증하는 과정에서 변조된 것을 감지했습니다."),
	/**E6022("`{0}` 을 생성하는 중 오류가 발생했습니다."),*/
	E6022("`{0}` 을 생성하는 중 오류가 발생했습니다."),
	/**E6023("`debug` 요청은 token 에 포함된 guid 로 재설정됩니다. hguid > tguid (`{0}` > `{1}`)"),*/
	E6023("`debug` 요청은 token 에 포함된 guid 로 재설정됩니다. hguid > tguid (`{0}` > `{1}`)"),


	/**E6101("세션이 만료 되었습니다"),*/
	E6101("세션이 만료 되었습니다"),
	/**E6102("중복로그인으로 세션이 만료 되었습니다."),*/
	E6102("중복로그인으로 세션이 만료 되었습니다."),
	/**E6103("세션 상태가 유효하지 않습니다."),*/
	E6103("세션 상태가 유효하지 않습니다."),
	/**E6104("로그인 사용자만 디버그 모드를 활성화 할 수 있습니다."),*/
	E6104("로그인 사용자만 디버깅 모드를 활성화 할 수 있습니다."),
	/**E6105("디버깅 모드를 활성화하는데 오류가 발생했습니다."),*/
	E6105("디버깅 모드를 활성화하는데 오류가 발생했습니다."),
	/**E6106("`session` 객체가 null 입니다."),*/
	E6106("`session` 객체가 null 입니다."),
	/**E6107("`session` 객체가 허용되지 않는 타입입니다. 타입={0}"),*/
	E6107("`session` 객체가 허용되지 않는 타입입니다. 타입={0}"),
	/**E6108("사용자 정보가 없습니다."),*/
	E6108("사용자 정보가 없습니다."),
	/**E6109("정의되지 않은 세션 상태입니다. ssTpcd={0}"),*/
	E6109("정의되지 않은 세션 상태입니다. ssTpcd={0}"),
	/**E6110("roleId 는 null 이 될 수 없습니다."),*/
	E6110("roleId 는 null 이 될 수 없습니다."),


	/**E6201("DTO암호화key(서버rsa)를 저장하는 중 오류가 발생했습니다."),*/
	E6201("DTO암호화key(서버rsa)를 저장하는 중 오류가 발생했습니다."),
	/**E6202("DTO암호화key(클라이언트aes)를 저장하는 중 오류가 발생했습니다."),*/
	E6202("DTO암호화key(클라이언트aes)를 저장하는 중 오류가 발생했습니다."),
	/**E6203("DTO암호화key를 조회하는 중 오류가 발생했습니다."),*/
	E6203("DTO암호화key를 조회하는 중 오류가 발생했습니다."),
	/**E6204("DTO암호화key의 서버private-key 가 비어있습니다."),*/
	E6204("DTO암호화key의 서버private-key 가 비어있습니다."),
	/**E6205("DTO암호화key의 클라이언트key 가 비어있습니다."),*/
	E6205("DTO암호화key의 클라이언트key 가 비어있습니다."),
	/**E6206("DTO암호화 처리 중 오류가 발생했습니다."),*/
	E6206("DTO암호화 처리 중 오류가 발생했습니다."),
	/**E6207("DTO필드복호화 처리 중 오류가 발생했습니다."),*/
	E6207("DTO필드복호화 처리 중 오류가 발생했습니다."),
	/**E6208("DTO암호화 필드 annotation 을 확인하는 중 오류가 발생했습니다."),*/
	E6208("DTO암호화 필드 annotation 을 확인하는 중 오류가 발생했습니다."),

	/**E6301("파일다운로드 시 `Content-Disposition` 에 파일명이 설정되지 않아 임의의값으로 파일명을 변경합니다. filename={}"),*/
	E6301("파일다운로드 시 `Content-Disposition` 에 파일명이 설정되지 않아 임의의값으로 파일명을 변경합니다. filename={}"),


	/**E7001("서버에 존재하지 않는 api 요청입니다. {0}"),*/
	E7001("서버에 존재하지 않는 api 요청입니다. {0}"),
	/**E7002("지원하지 않은 `Content-Type` 타입입니다. {0}"),*/
	E7002("지원하지 않은 `Content-Type` 타입입니다. {0}"),
	/**E7003("요청 api의 존재 여부를 체크하는 중 오류가 발생했습니다. {0}"),*/
	E7003("요청 api의 존재 여부를 체크하는 중 오류가 발생했습니다. {0}"),
	/**E7004("일시적으로 접근이 제한된 api 입니다."),*/
	E7004("일시적으로 접근이 제한된 api 입니다."),
	/**E7005("파일업로드 처리 중 오류가 발생했습니다."),*/
	E7005("파일업로드 처리 중 오류가 발생했습니다."),
	/**E7007("`{0}`에서 처리 중 오류가 발생했습니다."),*/
	E7007("`{0}`에서 처리 중 오류가 발생했습니다."),
	/**E7008("`{0}`에서 `{1}` 처리 중 오류가 발생했습니다."),*/
	E7008("`{0}`에서 `{1}` 처리 중 오류가 발생했습니다."),
	/**E7009("`{0}` 에서는 DEBUG 가 허용되지 않습니다."),*/
	E7009("`{0}` 에서는 DEBUG 가 허용되지 않습니다."),
	/**E7010("spring-web 에서 처리 중 오류가 발생했습니다. {0}"),*/
	E7010("spring-web 에서 처리 중 오류가 발생했습니다. {0}"),


	/**E8001("sql 실행 중 오류가 발생했습니다. `{0}`")*/
	E8001("sql 실행 중 오류가 발생했습니다. `{0}`"),
	/**E8002("SQL문에 오류가 있습니다. `{0}`"),*/
	E8002("SQL문에 오류가 있습니다. `{0}`"),
	/**E8003("SQL문에 오류가 있습니다. `{0}`"),*/
	E8003("SQL문에 오류가 있습니다. `{0}`"),
	/**E8004("중복된 레코드가 있습니다. `{0}`"),*/
	E8004("중복된 레코드가 있습니다. `{0}`"),
	/**E8005("@Mapper에 매핑된 sql이 없습니다. `{0}`"),*/
	E8005("@Mapper에 매핑된 sql이 없습니다. `{0}`"),
	/**E8006("sql에 파라미터 binding 이 잘못되었습니다. `{0}`"),*/
	E8006("sql에 파라미터 binding 이 잘못되었습니다. `{0}`"),
	/**E8007("`Map 타입`의 param-sql을 생성 중 binding 변수의 개수와 파라미터의 개수가 일치하지 않았습니다. `{0}`"),*/
	E8007("`Map 타입`의 param-sql을 생성 중 binding 변수의 개수와 파라미터의 개수가 일치하지 않았습니다. `{0}`"),
	/**E8008("`count-sql`을 실행하는데 오류가 발생했습니다. `{0}`"),*/
	E8008("`count-sql`을 실행하는데 오류가 발생했습니다. `{0}`"),


	/**E8101("`paging` 객체 생성에는 전체건수(`rowcount`)가 반드시 필요합니다."),*/
	E8101("`paging` 객체 생성에는 전체건수(`rowcount`)가 반드시 필요합니다."),
	/**E8102("`paging` 처리가 되는 sql 파라미터는 `{0}` 을 상속받은 VO 클래스 혹은 Map 클래스가 되어야 합니다."),*/
	E8102("`paging` 처리가 되는 sql 파라미터는 `{0}` 을 상속받은 VO 클래스 혹은 Map 클래스가 되어야 합니다."),


	/**E9001("유효하지 않은 json 문자열 입니다."),*/
	E9001("유효하지 않은 json 문자열 입니다."),
	/**E9002("유효하지 않은 파라미터 입니다."),*/
	E9002("유효하지 않은 파라미터 입니다."),
	/**E9003("날짜 형식에 오류가 있습니다. 문자열({0}), 포맷({1})"),*/
	E9003("날짜 형식에 오류가 있습니다. 문자열({0}), 포맷({1})"),
	/**E9998("오류 코드가 정의되지 않았습니다."),*/
	E9998("오류 코드가 정의되지 않았습니다."),
	/**E9999("정의되지 않은 오류가 발생했습니다. `{0}`");*/
	E9999("정의되지 않은 오류가 발생했습니다. `{0}`");

	private final String text;

	private KMessage(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}

	public static String get(KMessage object, Object ... args) {
		String result = "";
		if (args != null) {
			try {
				result = MessageFormat.format(object.text(), args);
			} catch(Exception e) {
				LoggerFactory.getLogger(KMessage.class).error("메시지 생성을 실패했습니다.", e);
			}
		} else {
			result = object.text();
		}
		return result;
	}

}
