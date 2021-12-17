package mgkim.proto.www.api.cmm.file.web;

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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.dto.KInDTO;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.property.KProperty;
import mgkim.framework.core.type.TUuidType;
import mgkim.framework.core.util.KFileUtil;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.proto.www.api.cmm.file.service.FileService;
import mgkim.proto.www.api.cmm.file.vo.FileVO;

@Api( tags = { KConstant.SWG_SERVICE_COMMON } )
@RestController
public class FileController {

	@Autowired
	private FileService fileService;

	@ApiOperation(value = "(file) 파일 다운로드")
	@RequestMapping(value = "/api/cmm/file/fileDownload", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<InputStreamResource> fileDownload(
			@RequestBody KInDTO<String> inDTO) throws Exception {
		ResponseEntity<InputStreamResource> result = null;

		// 파일 경로 조회
		String filepath = null;
		{
			String fileId = inDTO.getBody();
			FileVO param = new FileVO.Builder()
					.fileId(fileId)
					.build();
			FileVO fileVO = null;
			try {
				fileVO = fileService.selectFile(param);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
			filepath = fileVO.getSaveFpath();
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
	@RequestMapping(value = "/api/cmm/file/fgrpDownload/v1", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<InputStreamResource> fgrpDownload(
			@RequestParam(value = "fgrpId", required = true) String fgrpId,
			@RequestParam(value = "zipnm", required = false) String zipnm) throws Exception {
		ResponseEntity<InputStreamResource> result = null;

		// 파일 경로 조회
		List<FileVO> list = null;
		{
			FileVO param = new FileVO.Builder()
					.fgrpId(fgrpId)
					.build();
			try {
				list = fileService.selectFilegroup(param);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		// 파일 다운로드
		{
			zipnm = KStringUtil.nvl(zipnm, fgrpId+".zip");
			String txid = KContext.getT(AttrKey.TXID);
			String zipfpath = KConfig.TMPDIR + File.separator + txid + "_" + zipnm;
			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(new FileOutputStream(zipfpath));
				for(FileVO fileVO : list) {
					zos.putNextEntry(new ZipEntry(fileVO.getOrgFilenm()));
					byte[] buf = Files.readAllBytes(Paths.get(fileVO.getSaveFpath()));
					zos.write(buf);
				}
				zos.closeEntry();
			} catch(Exception e) {
				throw e;
			} finally {
				if(zos != null) {
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
	@RequestMapping(value = "/api/cmm/file/fgrpDownload/v2", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<FileSystemResource> fgrpDownloadv2(
			@RequestParam(value = "fgrpId", required = true) String fgrpId,
			@RequestParam(value = "zipnm", required = false) String zipnm) throws Exception {
		ResponseEntity<FileSystemResource> result = null;

		// 파일 경로 조회
		List<FileVO> list = null;
		{
			FileVO param = new FileVO.Builder()
					.fgrpId(fgrpId)
					.build();
			try {
				list = fileService.selectFilegroup(param);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		// 파일 다운로드
		{
			zipnm = KStringUtil.nvl(zipnm, fgrpId+".zip");
			String txid = KContext.getT(AttrKey.TXID);
			String zipfpath = KConfig.TMPDIR + File.separator + txid + "_" + zipnm;
			ZipOutputStream zos = null;
			try {
				zos = new ZipOutputStream(new FileOutputStream(zipfpath));
				for(FileVO fileVO : list) {
					zos.putNextEntry(new ZipEntry(fileVO.getOrgFilenm()));
					byte[] buf = Files.readAllBytes(Paths.get(fileVO.getSaveFpath()));
					zos.write(buf);
				}
				zos.closeEntry();
			} catch(Exception e) {
				throw e;
			} finally {
				if(zos != null) {
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
	@RequestMapping(value="/api/cmm/file/fgrpUpload", method=RequestMethod.POST)
	public @ResponseBody KOutDTO<List<FileVO>> fgrpUpload(
			@RequestPart(value = "attach", required = true) List<MultipartFile> attachList) throws Exception {
		KOutDTO<List<FileVO>> outDTO = new KOutDTO<List<FileVO>>();

		// 파라미터 검증
		{
			if(attachList == null || attachList.size() == 0) {
				//
				return null;
			}
		}

		// 파일 저장
		List<FileVO> list = new ArrayList<FileVO>();
		int cnt = attachList.size();
		for(int i=0; i<cnt; i++) {
			MultipartFile attach = attachList.get(i);
			FileVO fileVO = saveFile(attach);
			list.add(fileVO);
		}

		// 파일그룹 저장
		{
			String fgrpId = KStringUtil.createUuid(true, TUuidType.FGRPID);
			list.forEach(item -> {
				item.setFgrpId(fgrpId);
			});
			fileService.insertFilegroup(list);
		}

		outDTO.setBody(list);
		return outDTO;
	}

	@ApiOperation(value = "(file) 파일 업로드")
	@RequestMapping(value="/api/cmm/file/fileUpload", method=RequestMethod.POST)
	public @ResponseBody KOutDTO<FileVO> fileUpload(
			@RequestPart(value = "attach", required = true) MultipartFile attach) throws Exception {
		KOutDTO<FileVO> outDTO = new KOutDTO<FileVO>();
		FileVO fileVO = saveFile(attach);
		outDTO.setBody(fileVO);
		return outDTO;
	}

	private FileVO saveFile(MultipartFile attach) throws Exception {
		// 1) 파라미터 처리
		String orgFileName;
		String orgFileNm;
		String orgFileExt;

		// 1.1) 첨부파일 정보
		{
			if(attach == null) {
			}
			orgFileName = attach.getOriginalFilename();
			int pos = orgFileName.lastIndexOf(".");
			if(pos == -1) {
			}
			orgFileNm = orgFileName.substring(0, pos);
			orgFileExt = orgFileName.substring(pos + 1);
		}

		// 2) 첨부파일 저장
		String fileId = KStringUtil.createUuid(true, TUuidType.FILEID);
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
					String fmtStr = KProperty.getString("file.proto.www.board.path");
					savePath = formatter.format(fmtStr, new Object[] {currDate}).toString();
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
					String fmtStr = KProperty.getString("file.proto.www.board.name");
					saveFilenm = formatter.format(fmtStr, new Object[] {currDate, fileId, orgFileNm, orgFileExt}).toString();
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
		FileVO fileVO = null;
		{
			fileVO = new FileVO.Builder()
					.fileId(fileId)
					.orgFilenm(orgFileName)
					.saveFilenm(saveFilenm)
					.saveFpath(saveFpath)
					.fileExt(orgFileExt)
					.fileSize(file.length())
					.cksum(KFileUtil.cksum(file.getAbsolutePath()))
					.crptYn("N")
					.zipYn("N")
					.build();
			try {
				fileService.insertFile(fileVO);
			} catch(Exception e) {
				file.delete();
				throw e;
			}
		}

		return fileVO;
	}


}
