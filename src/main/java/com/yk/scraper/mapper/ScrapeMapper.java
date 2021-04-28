package com.yk.scraper.mapper;


import com.yk.scraper.entity.FileInfoEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScrapeMapper {

    // 자료에 대한 정보를 데이터베이스에 추가
    @Insert("INSERT INTO HK_FILEINFO( WRITEDATE, PART, TITLE, WRITER, PROVIDER, FILEURL ) VALUES( #{writeDate}, #{part}, #{title}, #{writer}, #{provider}, #{fileUrl} )")
    void insertInfo(FileInfoEntity fIleInfoEntity);


    @Delete("DELETE FROM HK_FILEINFO WHERE FILEURL = #{fileUrl}")
    void DeleteFileInfo(FileInfoEntity fileInfoEntity);

    @Select("SELECT IF((SELECT COUNT(*) FROM HK_FILEINFO where FILEURL = #{fileUrl}) = 0 , TRUE, FALSE)")
    boolean duplicateCheck(FileInfoEntity fileInfoEntity);

}
