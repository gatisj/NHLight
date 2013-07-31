package nodamushi.hl;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * 文字ファイルを全部読み込むユーティリティー
 * 
 * <br>
 * エンコードの判定にjuniversalchardet（http://code.google.com/p/juniversalchardet/）を利用しています。<br><br>
 * 
 * 引数のcharsetがnullの場合、juniversalchardetを利用して文字のエンコード判定をします。<br>
 * 読み込みに失敗したときはnullが返ります。
 * @author nodamushi
 *
 */
public class FullReadUtils{
    
    private FullReadUtils(){}
    
    //bufを取り出す為
    private static class BAOS extends ByteArrayOutputStream{
        public byte[] getData(){return buf;}
    }
    
    public static String read(String filename,String charset){
        return read(new File(filename),charset);
    }
    
    
    public static String read(File f,String charset){
        try(FileInputStream fin=new FileInputStream(f)){
            return read(fin,charset);
        } catch (FileNotFoundException e) {
            System.err.println("ファイルが見つかりませんでした。error message:"+e.getMessage());
        } catch (IOException e) {
            System.err.println("close()にて入出力例外が発生しました。 error message:"+e.getMessage());
        }
        return null;
    }
    
    
    public static String read(Path p ,String charset){
        try(InputStream in =Files.newInputStream(p)){
            return read(in,charset);
        } catch (IOException e) {
            System.err.println("close()にて入出力例外が発生しました。 error message:"+e.getMessage());
        }
        return null;
    }
    
    public static String read(URL url,String charset){
        try {
            return read(url.openConnection(),charset);
        } catch (IOException e) {
            System.err.println("URL#openConnection()にて入出力例外が発生しました。 error message:"+e.getMessage());
        }
        return null;
    }
    
    public static String read(URLConnection ucon,String charset){
        try(InputStream in=ucon.getInputStream()){
            return read(in,charset);
        }catch(UnknownServiceException e){
            System.err.println("URL 接続から返された MIME タイプが意味を持たない道の例外が発生しました。 error message:"+e.getMessage());
        }catch (IOException e) {
            System.err.println("URLConnection#getInputStream()にて入出力例外が発生しました。 error message"+e.getMessage());
        }
        return null;
    }
    
    public static String read(InputStream in,String charset){
        String ret=null;
        if(charset==null){
            ret= _read(in);
        }else try(BufferedReader r = new BufferedReader(new InputStreamReader(in,charset))){
            char[] buf = new char[1000];
            int read=0;
            StringBuilder sb = new StringBuilder();
            while((read=r.read(buf,0,1000)) !=-1){
                sb.append(buf,0,read);
            }
            ret=sb.toString();
        } catch (UnsupportedEncodingException e) {
            System.err.println(charset+"エンコードがサポートされていません。 error message:"+e.getMessage());
        } catch (IOException e) {
            System.err.println("InputStreamの読み込み中にエラーが発生しました。 error message:"+e.getMessage());
        }
        
        if(ret!=null && ret.charAt(0)==65279){//BOM削除
            return ret.substring(1);
        }
        
        return ret;
    }
    
    private static String _read(InputStream in){
        //文字コードがわからんので、とりま全部読み込み
        @SuppressWarnings("resource")//bytearrayなんだから閉じる必要有馬しぇん
        BAOS out = new BAOS();
        try(BufferedInputStream bio=new BufferedInputStream(in)){
            byte[] buf = new byte[1000];
            int read=0;
            while((read=bio.read(buf, 0, 1000))!=-1){
                out.write(buf, 0, read);
            }
        } catch (IOException e) {
            System.err.println("InputStreamの読み込み中にエラーが発生しました。 error message:"+e.getMessage());
            return null;
        }
        
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(out.getData(), 0, out.size());
        detector.dataEnd();
        String charset= detector.getDetectedCharset();
        if(charset==null){
            System.err.println("文字コードの判定を試みましたが、分かりませんでした。"
                    + "デフォルトのエンコードで文字列に変換します。(このメッセージは英字のみのファイルなどの場合もあり、必ずしもエラーではありません)");
            return new String(out.getData(), 0, out.size());
        }
        try {
            return new String(out.getData(), 0, out.size(), charset);
        } catch (UnsupportedEncodingException e) {//来ないはずだけどな～
            System.err.println("判定結果の"+charset+"は対応していません。デフォルトのエンコードで文字列に変換します");
            return new String(out.getData(), 0, out.size());
        }
    }
}