package mgkim.framework.online.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.type.KType.UuidType;
import mgkim.framework.core.util.KFileUtil;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.api.com.service.FileService;

@Api( tags = { KConstant.SWG_V1 } )
@RestController
public class V1FileController {

	@Autowired
	private FileService fileService;
	
	@ApiOperation(value = "(file) 파일 다운로드")
	@RequestMapping(value = "/v1/file/download/{fileId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<InputStreamResource> download_fileId(
			@KRequestMap HashMap<String, Object> inMap,
			@PathVariable(name = "fileId") String fileId) throws Exception {
		ResponseEntity<InputStreamResource> result = null;
		
		// 파일 경로 조회
		String filepath = null;
		{
			Map<String, Object> resultMap = null;
			try {
				resultMap = fileService.selectFile(inMap);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
			filepath = KStringUtil.nvl(resultMap.get("saveFpath"));
		}
		
		// 파일 다운로드
		{
			File file = new File(filepath);
			filepath = file.getAbsolutePath();
			String originFilename = file.getName();
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentLength(file.length());
				headers.add(KConstant.HK_CONTENT_DISPOSITION, KHttpUtil.getContentDisposition(originFilename));
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				InputStreamResource resource = new InputStreamResource(Files.newInputStream(file.toPath()));
				result = ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
			} catch(Exception e) {
				throw e;
			}
		}
		return result;
	}
	
	@ApiOperation(value = "(file) 파일그룹 다운로드 v1 (InputStreamResource) 응답 후 zip 파일을 삭제하지 않음")
	@RequestMapping(value = "/v1/filegrp/download1/{fgrpId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<InputStreamResource> download1_fgrpId(
			@KRequestMap HashMap<String, Object> inMap,
			@PathVariable(name = "fgrpId") String fgrpId,
			@RequestParam(value = "zipnm", required = false) String zipnm) throws Exception {
		ResponseEntity<InputStreamResource> result = null;

		// 파일 경로 조회
		List<Map<String, Object>> list = null;
		{
			Map<String, Object> fileMap = new HashMap<String, Object>();
			fileMap.put("fgrpId", fgrpId);
			try {
				list = fileService.selectFilegroup(fileMap);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		// 파일 다운로드
		{
			zipnm = KStringUtil.nvl(zipnm, fgrpId+".zip");
			String txid = KContext.getT(AttrKey.TXID);
			String zipfpath = KConstant.TMPDIR + File.separator + txid + "_" + zipnm;
			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(new FileOutputStream(zipfpath));
				for (Map<String, Object> fileVO : list) {
					String orgFilenm = KStringUtil.nvl(fileVO.get("orgFilenm"));
					zos.putNextEntry(new ZipEntry(orgFilenm));
					String saveFpath = KStringUtil.nvl(fileVO.get("saveFpath"));
					byte[] buf = Files.readAllBytes(Paths.get(saveFpath));
					zos.write(buf);
				}
				zos.closeEntry();
			} catch(Exception e) {
				throw e;
			} finally {
				if (zos != null) {
					zos.close();
				}
			}

			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentLength(new File(zipfpath).length());
				headers.add(KConstant.HK_CONTENT_DISPOSITION, KHttpUtil.getContentDisposition(zipnm));
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				InputStreamResource resource = new InputStreamResource(Files.newInputStream(new File(zipfpath).toPath()));
				result = ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
			} catch(Exception e) {
				throw e;
			}
		}
		return result;
	}

	@ApiOperation(value = "(file) 파일그룹 다운로드 v2 (FileSystemResource) 응답 후 zip 파일 삭제")
	@RequestMapping(value = "/v1/filegrp/download2/{fgrpId}", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<FileSystemResource> download2_fgrpId(
			@KRequestMap HashMap<String, Object> inMap,
			@PathVariable(name = "fgrpId") String fgrpId,
			@RequestParam(value = "zipnm", required = false) String zipnm) throws Exception {
		ResponseEntity<FileSystemResource> result = null;

		// 파일 경로 조회
		List<Map<String, Object>> list = null;
		{
			Map<String, Object> fileMap = new HashMap<String, Object>();
			fileMap.put("fgrpId", fgrpId);
			try {
				list = fileService.selectFilegroup(fileMap);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		// 파일 다운로드
		{
			zipnm = KStringUtil.nvl(zipnm, fgrpId+".zip");
			String txid = KContext.getT(AttrKey.TXID);
			String zipfpath = KConstant.TMPDIR + File.separator + txid + "_" + zipnm;
			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(new FileOutputStream(zipfpath));
				for (Map<String, Object> fileMap : list) {
					String orgFilenm = KStringUtil.nvl(fileMap.get("orgFilenm"));
					zos.putNextEntry(new ZipEntry(orgFilenm));
					String saveFpath = KStringUtil.nvl(fileMap.get("saveFpath"));
					byte[] buf = Files.readAllBytes(Paths.get(saveFpath));
					zos.write(buf);
				}
				zos.closeEntry();
			} catch(Exception e) {
				throw e;
			} finally {
				if (zos != null) {
					zos.close();
				}
			}

			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentLength(new File(zipfpath).length());
				headers.add(KConstant.HK_CONTENT_DISPOSITION, KHttpUtil.getContentDisposition(zipnm));
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				File zipfile = new File(zipfpath);
				FileSystemResource resource = new FileSystemResource(zipfile) {
					@Override
					public InputStream getInputStream() throws IOException {
						return new FileInputStream(zipfile) {
							@Override
							public void close() throws IOException {
								super.close();
								Files.delete(zipfile.toPath());
							}
						};
					}
				};
				result = ResponseEntity.status(HttpStatus.OK).headers(headers).body(resource);
			} catch(Exception e) {
				throw e;
			}
		}
		return result;
	}

	@ApiOperation(value = "(file) 파일그룹 업로드")
	@RequestMapping(value="/v1/filegrp/upload", method=RequestMethod.PUT)
	public @ResponseBody KOutDTO<List<Map<String, Object>>> upload_filegrp(
			@RequestPart(value = "attach", required = true) List<MultipartFile> attachList) throws Exception {
		KOutDTO<List<Map<String, Object>>> outDTO = new KOutDTO<List<Map<String, Object>>>();
		
		// 파라미터 검증
		{
			if (attachList == null || attachList.size() == 0) {
				//
				return null;
			}
		}
		
		// 파일 저장
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int cnt = attachList.size();
		for (int i=0; i<cnt; i++) {
			MultipartFile attach = attachList.get(i);
			Map<String, Object> fileVO = saveFile(attach);
			list.add(fileVO);
		}

		// 파일그룹 저장
		{
			String fgrpId = KStringUtil.createUuid(true, UuidType.FGRPID);
			list.forEach(item -> {
				item.put("fgrpId", fgrpId);
			});
			fileService.insertFilegroup(list);
		}

		outDTO.setBody(list);
		return outDTO;
	}
	
	@ApiOperation(value = "(file) 파일 업로드")
	@RequestMapping(value="/v1/file/upload", method=RequestMethod.PUT)
	public @ResponseBody KOutDTO<Map<String, Object>> upload_file(
			@RequestPart(value = "attach", required = true) MultipartFile attach) throws Exception {
		KOutDTO<Map<String, Object>> outDTO = new KOutDTO<Map<String, Object>>();
		Map<String, Object> fileMap = saveFile(attach);
		outDTO.setBody(fileMap);
		return outDTO;
	}
	
	@Value("${file.upload.dirpath}")
	private String dirpath;
	@Value("${file.upload.filename}")
	private String filename;
	
	private Map<String, Object> saveFile(MultipartFile attach) throws Exception {
		// 1) 파라미터 처리
		String orgFilenm;
		String _orgFile;
		String _orgFileExt;

		// 1.1) 첨부파일 정보
		{
			if (attach == null) {
			}
			orgFilenm = attach.getOriginalFilename();
			int pos = orgFilenm.lastIndexOf(".");
			if (pos == -1) {
			}
			_orgFile = orgFilenm.substring(0, pos);
			_orgFileExt = orgFilenm.substring(pos + 1);
		}

		// 2) 첨부파일 저장
		String fileId = KStringUtil.createUuid(true, UuidType.FILEID);
		String savePath = null;       // 첨부파일 디렉토리
		String saveFilenm = null;     // 첨부파일 파일명
		String saveFpath = null;      // 첨부파일 디렉토리+"/"+파일명
		File file = null;
		{
			final Date currDate = new Date();
			// 2.1) 첨부파일 디렉토리 확인
			{
				Formatter formatter = new Formatter();
				try {
					savePath = formatter.format(dirpath, new Object[] {currDate}).toString();
				} catch(Exception e) {
					throw e;
				} finally {
					formatter.close();
				}
			}

			// 2.2) 첨부파일 파일명 확인
			{
				Formatter formatter = new Formatter();
				try {
					saveFilenm = formatter.format(filename, new Object[] {currDate, fileId, _orgFile, _orgFileExt}).toString();
				} catch(Exception e) {
					throw e;
				} finally {
					formatter.close();
				}
				saveFpath = savePath + "/" + saveFilenm;
			}

			// 2.3) 첨부파일 저장 (디스크)
			{
				file = new File(savePath + "/" + saveFilenm);
				try {
					FileUtils.writeByteArrayToFile(file, attach.getBytes());
				} catch(Exception e) {
					throw e;
				}
			}
		}

		// 3) 첨부파일 등록 (DB)
		Map<String, Object> fileMap = new HashMap<String, Object>();
		{
			fileMap.put("fileId", fileId);
			fileMap.put("orgFilenm", orgFilenm);
			fileMap.put("saveFilenm", saveFilenm);
			fileMap.put("saveFpath", saveFpath);
			fileMap.put("orgFileExt", _orgFileExt);
			fileMap.put("fileSize", file.length());
			fileMap.put("cksum", KFileUtil.cksum(file.getAbsolutePath()));
			fileMap.put("crptYn", "N");
			fileMap.put("zipYn", "N");
			try {
				fileService.insertFile(fileMap);
			} catch(Exception e) {
				file.delete();
				throw e;
			}
		}

		return fileMap;
	}


}
