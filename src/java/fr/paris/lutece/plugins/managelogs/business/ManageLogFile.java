package fr.paris.lutece.plugins.managelogs.business;

import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

public class ManageLogFile
{
    private Path _path;
    private String _fileName;
    private long _size;
    private String _fileSize;
    private Integer _itemNumber;

    public ManageLogFile( Path path)
    {
        this._path = path;
        if (path != null) {
            this._fileName = path.getFileName().toString();
            this._size = FileUtils.sizeOf(path.toFile() );
            this._fileSize = FileUtils.byteCountToDisplaySize( _size );
        }
    }

    public ManageLogFile( Path path, int itemNumber )
    {
        this._path = path;
        if (path != null) {
            this._fileName = path.getFileName().toString();
            this._size = FileUtils.sizeOf(path.toFile() );
            this._fileSize = FileUtils.byteCountToDisplaySize( _size );
        }
        this._itemNumber = itemNumber;
    }

    public Path getPath( )
    {
        return _path;
    }

    public void setPath( Path path )
    {
        this._path = path;
    }

    public String getFileName( )
    {
        return _fileName;
    }

    public void setFileName( String fileName )
    {
        this._fileName = fileName;
    }

    public long getSize( )
    {
        return _size;
    }

    public void setSize( long size )
    {
        this._size = size;
    }

    public String getFileSize( )
    {
        return _fileSize;
    }

    public void setFileSize( String fileSize )
    {
        this._fileSize = fileSize;
    }

    public Integer getItemNumber( )
    {
        return _itemNumber;
    }

    public void setItemNumber( Integer itemNumber )
    {
        this._itemNumber = itemNumber;
    }
}
