#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include "zz_app_gif2mp4_Utils.h"
#include "android/log.h"
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/opt.h"
#include "libswscale/swscale.h"
#include "math.h"
#define SUCCESSCODE	233
#define MP4TIMESCALE	60
#define LOGD( ... )	__android_log_print( ANDROID_LOG_DEBUG, "gif2mp4_zz", __VA_ARGS__ )
#define LOGE( ... )	__android_log_print( ANDROID_LOG_ERROR, "gif2mp4_zz", __VA_ARGS__ )
enum GIF2MP4ErrorType {
  GIF2MP4_UNKNOWN_ERROR,
  GIF2MP4_H264_NOTFOUND,
  GIF2MP4_NOT_ACTION
};
jint progress;
jstring charTojstring( JNIEnv* env, const char* pat )
{
  /* 定义java String类 strClass */
  jclass strClass = (*env)->FindClass( env, "Ljava/lang/String;" );
  /* 获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String */
  jmethodID ctorID = (*env)->GetMethodID( env, strClass, "<init>", "([BLjava/lang/String;)V" );
  /* 建立byte数组 */
  jbyteArray bytes = (*env)->NewByteArray( env, strlen( pat ) );
  /* 将char* 转换为byte数组 */
  (*env)->SetByteArrayRegion( env, bytes, 0, strlen( pat ), (jbyte *) pat );
  /* 设置String, 保存语言类型,用于byte数组转换至String时的参数 */
  jstring encoding = (*env)->NewStringUTF( env, "UTF-8" );
  /* 将byte数组转换为java String,并输出 */
  return( (jstring) (*env)->NewObject( env, strClass, ctorID, bytes, encoding ) );
}


char* jstringToChar( JNIEnv* env, jstring jstr )
{
  char		* rtn		= NULL;
  jclass		clsstring	= (*env)->FindClass( env, "java/lang/String" );
  jstring		strencode	= (*env)->NewStringUTF( env, "UTF-8" );
  jmethodID	mid		= (*env)->GetMethodID( env, clsstring, "getBytes", "(Ljava/lang/String;)[B" );
  jbyteArray	barr		= (jbyteArray) (*env)->CallObjectMethod( env, jstr, mid, strencode );
  jsize		alen		= (*env)->GetArrayLength( env, barr );
  jbyte		* ba		= (*env)->GetByteArrayElements( env, barr, JNI_FALSE );
  if ( alen > 0 )
  {
    rtn = (char *) malloc( alen + 1 );
    memcpy( rtn, ba, alen );
    rtn[alen] = 0;
  }
  (*env)->ReleaseByteArrayElements( env, barr, ba, 0 );
  return(rtn);
}


JNIEXPORT void JNICALL Java_zz_app_gif2mp4_Utils_welcome
( JNIEnv * env, jclass cls )
{
  LOGD( "Hello FFMpeg!" );
}




JNIEXPORT jboolean JNICALL Java_zz_app_gif2mp4_Utils_checkgif
( JNIEnv * env, jclass cls, jstring path )
{
  float		rate = 1;
  float		realspeed;
  jboolean	retval		= JNI_TRUE;
  char		*input		= jstringToChar( env, path );
  AVFormatContext * inputFmtCtx	= avformat_alloc_context();
  int		rtn		= avformat_open_input( &inputFmtCtx, input, NULL, NULL );
  if ( rtn < 0 )
  {
    retval = JNI_FALSE;
    goto END;
  }
  avformat_find_stream_info( inputFmtCtx, NULL );
  if ( rtn < 0 )
  {
    retval = JNI_FALSE;
    goto END;
  }
  int	vsnb	= -1;
  int	i	= 0, j = 0, k = 0;
  float	sum	= 0;
  for (; i < inputFmtCtx->nb_streams; i++ )
  {
    if ( inputFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO )
    {
      vsnb = i;
      break;
    }
  }
  if ( vsnb != -1 )
  {
    if ( inputFmtCtx->streams[vsnb]->avg_frame_rate.den == 0 || realspeed == 0 )
    {
      retval = JNI_FALSE;
      goto END;
    }
  } else retval = JNI_FALSE;
END :
  free( input );
  avformat_free_context( inputFmtCtx );
  return(retval);
}


JNIEXPORT jint JNICALL Java_zz_app_gif2mp4_Utils_gifframes
( JNIEnv * env, jclass cls, jstring path )
{
  char		*input		= jstringToChar( env, path );
  AVFormatContext * inputFmtCtx	= avformat_alloc_context();
  int		rtn		= avformat_open_input( &inputFmtCtx, input, NULL, NULL );
  if ( rtn < 0 )
    return(-1);
  avformat_find_stream_info( inputFmtCtx, NULL );
  if ( rtn < 0 )
    return(-1);
  int	vsnb	= -1;
  int	i	= 0;
  for (; i < inputFmtCtx->nb_streams; i++ )
  {
    if ( inputFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO )
    {
      vsnb = i;
      break;
    }
  }
  AVCodecContext *decoderCtx = avcodec_alloc_context3( NULL );
  if ( vsnb != -1 )
  {
    AVCodec* codec = avcodec_find_decoder( inputFmtCtx->streams[vsnb]->codecpar->codec_id );
    avcodec_parameters_to_context( decoderCtx, inputFmtCtx->streams[vsnb]->codecpar );
    avcodec_open2( decoderCtx, codec, NULL );
  }
  AVPacket pkt;
  i = 0;
  int	ret;
  AVFrame *frame = av_frame_alloc();

  while ( av_read_frame( inputFmtCtx, &pkt ) >= 0 )
  {
    if ( pkt.stream_index == vsnb )
    {
      ret = avcodec_send_packet( decoderCtx, &pkt );
      while ( ret >= 0 )
      {
        ret = avcodec_receive_frame( decoderCtx, frame );
        if ( ret == AVERROR( EAGAIN ) || ret == AVERROR_EOF )
          continue;
        i++;
      }
    }
  }
  avcodec_free_context( &decoderCtx );
  av_frame_free( &frame );
  avformat_free_context( inputFmtCtx );
  free( input );
  return(i);
}


JNIEXPORT jfloat JNICALL Java_zz_app_gif2mp4_Utils_gifavgrate
( JNIEnv * env, jclass cls, jstring path )
{
  float		rate		= 1;
  char		*input		= jstringToChar( env, path );
  AVFormatContext * inputFmtCtx	= avformat_alloc_context();
  avformat_open_input( &inputFmtCtx, input, NULL, NULL );
  avformat_find_stream_info( inputFmtCtx, NULL );
  int	vsnb	= -1;
  int	i	= 0, j = 0, k = 0;
  float	sum	= 0;
  for (; i < inputFmtCtx->nb_streams; i++ )
  {
    if ( inputFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO )
    {
      vsnb = i;
      break;
    }
  }
  if ( vsnb != -1 )
  {
    rate = ( ( (float) inputFmtCtx->streams[vsnb]->avg_frame_rate.num) / inputFmtCtx->streams[vsnb]->avg_frame_rate.den);
  }

  free( input );
  avformat_free_context( inputFmtCtx );
  return(rate);
}


JNIEXPORT jint JNICALL Java_zz_app_gif2mp4_Utils_gif2mp4
( JNIEnv * env, jclass cls, jstring inputpath, jstring outputpath, jint enc, jdouble bitr, jdouble time, jint framecnt )
{
  progress = 0;
  double	rate = 1;
  float	realspeed;
  int	rtn;
  char	*input	= jstringToChar( env, inputpath );
  char	*output = jstringToChar( env, outputpath );
  LOGD( "input = %s", input );
  LOGD( "output = %s", output );
  AVFormatContext * inputFmtCtx	= avformat_alloc_context();
  AVFormatContext * outputFmtCtx	= avformat_alloc_context();
  LOGD( "AVFormatContext create successfully" );
  rtn = avformat_open_input( &inputFmtCtx, input, NULL, NULL );
  if ( rtn < 0 )
    return(GIF2MP4_UNKNOWN_ERROR);
  LOGD( "avformat_open_input  successfully" );
  avformat_find_stream_info( inputFmtCtx, NULL );
  if ( rtn < 0 )
    return(GIF2MP4_UNKNOWN_ERROR);
  LOGD( "avformat_find_stream_info successfully" );
  int	vsnb	= -1;
  int	i	= 0, j = 0, k = 0;
  float	sum	= 0;
  for (; i < inputFmtCtx->nb_streams; i++ )
  {
    if ( inputFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO )
    {
      vsnb = i;
      break;
    }
  }
  AVCodecContext *decoderCtx = avcodec_alloc_context3( NULL );
  if ( vsnb != -1 )
  {
    LOGD( "input type = %d", inputFmtCtx->streams[vsnb]->codecpar->codec_id );
    LOGD( "frame_rate_num = %d frame_rate_den = %d", inputFmtCtx->streams[vsnb]->avg_frame_rate.num, inputFmtCtx->streams[vsnb]->avg_frame_rate.den );
    if ( inputFmtCtx->streams[vsnb]->avg_frame_rate.den == 0 )
    {
      return(GIF2MP4_NOT_ACTION);
    }

    rate		= framecnt / time;
    realspeed	= MP4TIMESCALE / rate;
    LOGD( "rate = %f", rate );
    LOGD( "realspeed = %f", realspeed );
    if ( realspeed == 0 )
    {
      return(GIF2MP4_NOT_ACTION);
    }

    AVCodec* codec = avcodec_find_decoder( inputFmtCtx->streams[vsnb]->codecpar->codec_id );
    avcodec_parameters_to_context( decoderCtx, inputFmtCtx->streams[vsnb]->codecpar );
    avcodec_open2( decoderCtx, codec, NULL );

    LOGD( "open decoder  successfully" );
  }
  avformat_alloc_output_context2( &outputFmtCtx, NULL, NULL, output );
  AVStream	*stream		= avformat_new_stream( outputFmtCtx, NULL );
  AVCodecContext	* encoderCtx	= avcodec_alloc_context3( NULL );
  if ( enc == 0 )
  {
    LOGD( "codec id = %d", outputFmtCtx->oformat->video_codec );
    AVCodec* codec = avcodec_find_encoder( outputFmtCtx->oformat->video_codec );
    encoderCtx->codec_id	= outputFmtCtx->oformat->video_codec;
    encoderCtx->codec_type	= AVMEDIA_TYPE_VIDEO;
    encoderCtx->width	= decoderCtx->width / 2 * 2;
    encoderCtx->height	= decoderCtx->height / 2 * 2;
    LOGD( "input width=%d height=%d output width=%d height=%d", decoderCtx->width, decoderCtx->height, encoderCtx->width, encoderCtx->height );
    encoderCtx->bit_rate		= (int) bitr;
    encoderCtx->qcompress		= 0.6;
    encoderCtx->max_qdiff		= 3;
    encoderCtx->qmin		= 10;
    encoderCtx->qmax		= 51;
    encoderCtx->me_range		= 16;
    encoderCtx->gop_size		= 12;
    encoderCtx->max_b_frames	= 1;
    encoderCtx->thread_count	= 4;
    encoderCtx->pix_fmt		= AV_PIX_FMT_YUV420P;
    AVRational r = { 1, MP4TIMESCALE };
    encoderCtx->time_base = r;
    unsigned char sps_pps[23] = { 0x00, 0x00, 0x00, 0x01, 0x67, 0x42, 0x00, 0x0a, 0xf8, 0x0f, 0x00, 0x44, 0xbe, 0x8,
                                  0x00, 0x00, 0x00, 0x01, 0x68, 0xce, 0x38, 0x80
                                };
    encoderCtx->extradata_size	= 23;
    encoderCtx->extradata		= (uint8_t *) av_malloc( 23 + AV_INPUT_BUFFER_PADDING_SIZE );
    if ( encoderCtx->extradata == NULL )
    {
      LOGE( "could not av_malloc the video params extradata!" );
      return(GIF2MP4_UNKNOWN_ERROR);
    }
    memcpy( encoderCtx->extradata, sps_pps, 23 );

    av_opt_set( encoderCtx->priv_data, "tune", "zerolatency", 0 );
    av_opt_set( encoderCtx->priv_data, "preset", "superfast", 0 );
    avcodec_parameters_from_context( stream->codecpar, encoderCtx );
    rtn = avcodec_open2( encoderCtx, codec, NULL );
    if ( rtn >= 0 )
      LOGD( "open encoder  successfully" );
    else {
      LOGE( "open encoder  failed(H264)" );
      return(GIF2MP4_H264_NOTFOUND);
    }
  } else {
    LOGD( "codec id = %d", AV_CODEC_ID_MPEG4 );
    AVCodec* codec = avcodec_find_encoder( AV_CODEC_ID_MPEG4 );
    encoderCtx->codec_id	= AV_CODEC_ID_MPEG4;
    encoderCtx->codec_type	= AVMEDIA_TYPE_VIDEO;
    encoderCtx->width	= decoderCtx->width / 2 * 2;
    encoderCtx->height	= decoderCtx->height / 2 * 2;
    LOGD( "input width=%d height=%d output width=%d height=%d", decoderCtx->width, decoderCtx->height, encoderCtx->width, encoderCtx->height );
    encoderCtx->bit_rate		= (int) bitr;
    encoderCtx->qcompress		= 0.6;
    encoderCtx->max_qdiff		= 3;
    encoderCtx->qmin		= 10;
    encoderCtx->qmax		= 51;
    encoderCtx->me_range		= 16;
    encoderCtx->gop_size		= 12;
    encoderCtx->max_b_frames	= 1;
    encoderCtx->thread_count	= 4;
    encoderCtx->pix_fmt		= AV_PIX_FMT_YUV420P;
    AVRational r = { 1, MP4TIMESCALE };
    encoderCtx->time_base = r;

    av_opt_set( encoderCtx->priv_data, "tune", "zerolatency", 0 );
    av_opt_set( encoderCtx->priv_data, "preset", "superfast", 0 );
    avcodec_parameters_from_context( stream->codecpar, encoderCtx );
    rtn = avcodec_open2( encoderCtx, codec, NULL );
    if ( rtn >= 0 )
      LOGD( "open encoder  successfully" );
    else {
      LOGE( "open encoder  failed(MPEG4)" );
      return(GIF2MP4_UNKNOWN_ERROR);
    }
  }
  if ( !(outputFmtCtx->oformat->flags & AVFMT_NOFILE) )
  {
    avio_open( &outputFmtCtx->pb, output, AVIO_FLAG_WRITE );
  }
  AVDictionary *dic = NULL;
  av_dict_set_int( &dic, "video_track_timescale", MP4TIMESCALE, 0 );
  av_dict_set( &dic, "movflags", "faststart", 0 );
  rtn = avformat_write_header( outputFmtCtx, &dic );
  if ( rtn < 0 )
    return(GIF2MP4_UNKNOWN_ERROR);
  LOGD( "avformat_write_header  successfully" );
  i = 0;
  int		ret, ret2, ret3;
  AVPacket	pkt,pkt2;
  AVFrame		*frame		= av_frame_alloc();
  AVFrame		* frame2	= av_frame_alloc();
  av_new_packet( &pkt2, decoderCtx->width * decoderCtx->height * 3 );
  struct SwsContext* swsctx = sws_getContext( decoderCtx->width, decoderCtx->height, decoderCtx->pix_fmt,
                              decoderCtx->width % 4 != 0 ? encoderCtx->width + 4 : encoderCtx->width, decoderCtx->height % 4 != 0 ? encoderCtx->height + 4 : encoderCtx->height,
                              encoderCtx->pix_fmt, SWS_BICUBIC, NULL, NULL, NULL );
  while ( av_read_frame( inputFmtCtx, &pkt ) >= 0 )
  {
    LOGD( "av_read_frame pkt size = %d", pkt.size );
    if ( pkt.stream_index == vsnb )
    {
      ret = avcodec_send_packet( decoderCtx, &pkt );
      LOGD( "avcodec_send_packet" );
      while ( ret >= 0 )
      {
        ret = avcodec_receive_frame( decoderCtx, frame );
        if ( ret == AVERROR( EAGAIN ) || ret == AVERROR_EOF )
          continue;
        LOGD( "avcodec_receive_frame" );
        frame2->width	= encoderCtx->width;
        frame2->height	= encoderCtx->height;
        frame2->format	= encoderCtx->pix_fmt;
        i++;
        progress = i * 100 / framecnt;
        jclass		clazz		= (*env)->FindClass( env, "zz/app/gif2mp4/Utils" );
        jmethodID	methodID	= (*env)->GetStaticMethodID( env, clazz, "setProgress2", "(I)V" );
        (*env)->CallStaticVoidMethod( env, cls, methodID, progress );
        LOGD( "pix width=%d height=%d format=%d", frame2->width, frame2->height, frame2->format );
        av_frame_get_buffer( frame2, 32 );
        sws_scale( swsctx, (const uint8_t * const *) frame->data, frame->linesize, 0, frame2->height, frame2->data, frame2->linesize );
        av_frame_unref(frame);
        frame2->pts	= (int) sum;
        sum		+= realspeed;
        ret2		= avcodec_send_frame( encoderCtx, frame2 );
        av_frame_unref(frame2);
        if ( ret2 == AVERROR( EAGAIN ) || ret == AVERROR_EOF )
          continue;
        LOGD( "avcodec_send_frame i = %d", (int) sum );
        ret2 = avcodec_receive_packet( encoderCtx, &pkt2 );
        if ( ret2 == AVERROR( EAGAIN ) || ret2 == AVERROR_EOF )
          continue;
        LOGD( "avcodec_receive_packet size=%d", pkt2.size );
        pkt2.stream_index = stream->index;
        av_write_frame( outputFmtCtx, &pkt2 );
        av_packet_unref(&pkt2);
        LOGD( "av_write_frame" );
      }
    }
    av_packet_unref(&pkt);
  }
  ret2 = 0;
  while ( ret2 >= 0 )
  {
    ret2 = avcodec_send_frame( encoderCtx, NULL );
    if ( ret2 == AVERROR( EAGAIN ) || ret == AVERROR_EOF )
      continue;
    sum += realspeed;
    LOGD( "avcodec_send_frame fake i = %d", (int) sum );
    ret2 = avcodec_receive_packet( encoderCtx, &pkt2 );
    if ( ret2 == AVERROR( EAGAIN ) || ret2 == AVERROR_EOF )
      continue;
    LOGD( "avcodec_receive_packet size=%d", pkt2.size );
    pkt2.stream_index = stream->index;
    av_write_frame( outputFmtCtx, &pkt2 );
    av_packet_unref(&pkt2);
    LOGD( "av_write_frame" );
  }

  rtn = av_write_trailer( outputFmtCtx );
  if ( rtn < 0 )
    return(GIF2MP4_UNKNOWN_ERROR);
  LOGD( "av_write_trailer complete successfully" );
  avio_close( outputFmtCtx->pb );
  sws_freeContext( swsctx );
  avcodec_free_context( &decoderCtx );
  avcodec_free_context( &encoderCtx );
  LOGD( "avcodec_free_context complete successfully" );
  av_frame_free( &frame );
  av_frame_free( &frame2 );
  LOGD( "av_frame_free complete successfully" );
  avformat_free_context( inputFmtCtx );
  avformat_free_context( outputFmtCtx );
  LOGD( "avformat_free_context complete successfully" );
  av_dict_free( &dic );
  LOGD( "av_dict_free complete successfully" );
  free( input );
  free( output );
  return(SUCCESSCODE);
}

JNIEXPORT jint JNICALL Java_zz_app_gif2mp4_Utils_mp42gif
(JNIEnv * env, jclass cls, jstring mp4path, jstring gifpath, jint _fps, jint _rotate, jint _width, jint _height, jdouble _start, jdouble _end)
{
  progress = 0;
  char	*input	= jstringToChar( env, mp4path );
  char	*output = jstringToChar( env, gifpath );
  AVFormatContext *infmtctx = NULL;
  AVFormatContext *outfmtctx = NULL;
  AVCodecContext *decctx = NULL;
  AVCodecContext *encctx = NULL;
  int framecnt;
  double time;
  int ret, ret2;
  int video_index;
  double start_,end_;
  int rotate=0;
  double fps;
  int output_w, output_h;
  int i,j,ii,jj;
  ret = avformat_open_input(&infmtctx,input, NULL, NULL);
  if (ret < 0)
    goto FAIL;
  ret = avformat_find_stream_info(infmtctx, NULL);
  if (ret < 0)
    goto FAIL;

  for ( i = 0; i < infmtctx->nb_streams; i++)
  {
    if (infmtctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO)
    {
      video_index = i;
      break;

    }
  }
  AVStream* video_stream = infmtctx->streams[video_index];
  double video_fps = av_q2d(video_stream->avg_frame_rate);

  if (_fps == -1)fps = video_fps;
  else fps=_fps;
  double ptsdelta = 100 / video_fps;
  double frame_interval = video_fps / fps;
  AVRational video_time_base = video_stream->time_base;
  if(_start==-1)start_=0;
  else start_=_start;
  if(_end==-1)end_=video_stream->nb_frames/av_q2d(video_time_base);
  else end_=_end;
  AVDictionaryEntry *m = NULL;
  if((m = av_dict_get(video_stream->metadata, "rotate", m, AV_DICT_IGNORE_SUFFIX)) != NULL) {
    rotate=atoi(m->value);
  }
  rotate+=_rotate;
  if (rotate < 0)
    rotate += 360;
  else if(rotate>360)
    rotate -=360;
  framecnt = video_stream->nb_frames;
  time = video_stream->duration*av_q2d(video_stream->time_base);
  AVCodec *c = avcodec_find_decoder(video_stream->codecpar->codec_id);//H264?
  decctx = avcodec_alloc_context3(NULL);
  avcodec_parameters_to_context(decctx, video_stream->codecpar);
  ret = avcodec_open2(decctx, c, NULL);
  if (ret < 0)
    goto FAIL;
  if (_width == -1)output_w = decctx->width;
  else output_w=_width;
  if (_height == -1)output_h = decctx->height;
  else output_h=_height;
  avformat_alloc_output_context2(&outfmtctx, NULL, NULL,output);
  AVStream* out_stream = avformat_new_stream(outfmtctx, NULL);
  AVCodec* c2 = avcodec_find_encoder(outfmtctx->oformat->video_codec);
  encctx = avcodec_alloc_context3(NULL);

  encctx->width = output_w;
  encctx->height = output_h;

  encctx->codec_id = outfmtctx->oformat->video_codec;
  encctx->codec_type = AVMEDIA_TYPE_VIDEO;
  encctx->pix_fmt = AV_PIX_FMT_BGR8;
  encctx->time_base = (AVRational) {
    1,1
  };
  avcodec_parameters_from_context(out_stream->codecpar,encctx);
  ret = avcodec_open2(encctx, c2, NULL);
  if (ret < 0)
    goto FAIL;

  if (!(outfmtctx->flags&AVFMT_NOFILE))
  {
    ret = avio_open(&outfmtctx->pb, output, AVIO_FLAG_WRITE);
    if (ret < 0)
      goto FAIL;
  }
  AVPacket packet;
  AVPacket packet2;
  av_new_packet(&packet2, encctx->width*encctx->height * 3);
  AVFrame *frame = av_frame_alloc();
  AVFrame *frame2 = av_frame_alloc();
  AVFrame *frame3 = av_frame_alloc();
  AVFrame *frame4 = av_frame_alloc();

  struct SwsContext *swsctx = sws_getContext(decctx->width, decctx->height, decctx->pix_fmt, decctx->width, decctx->height, encctx->pix_fmt, SWS_BICUBIC, NULL, NULL, NULL);
  struct SwsContext *swsctx2 = NULL;
  if(rotate==0||rotate==180)
    swsctx2 = sws_getContext(decctx->width, decctx->height, AV_PIX_FMT_BGR8, output_w, output_h, AV_PIX_FMT_BGR8, SWS_BICUBIC, NULL, NULL, NULL);
  else
    swsctx2 = sws_getContext(decctx->height, decctx->width, AV_PIX_FMT_BGR8, output_w, output_h, AV_PIX_FMT_BGR8, SWS_BICUBIC, NULL, NULL, NULL);
  ret = 0;
  i=0;
  int frmcnt = 0;
  double frame_num = 0;
  double pts_sum = 0;
  double frame_sum = 0;
  ret=avformat_write_header(outfmtctx, NULL);
  if (ret < 0)
    goto FAIL;
  av_seek_frame(infmtctx, video_index, (start_-5<0?start_:start_-5) / av_q2d(video_time_base), 0);
  while (av_read_frame(infmtctx, &packet) >= 0) {

    if (packet.stream_index == video_index) {
      int ret = avcodec_send_packet(decctx, &packet);

      if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
        continue;

      ret = avcodec_receive_frame(decctx, frame);

      if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
      {
        av_frame_unref(frame);
        continue;
      }
      double current_time = frame->pts*av_q2d(video_time_base);

      if (current_time < start_)
      {
        av_frame_unref(frame);
        continue;
      }
      else if (current_time > end_)
      {
        av_frame_unref(frame);
        goto FLUSH;

      }
      progress = (current_time-start_) * 100 / (end_-start_);
      jclass		clazz		= (*env)->FindClass( env, "zz/app/gif2mp4/Utils" );
      jmethodID	methodID	= (*env)->GetStaticMethodID( env, clazz, "setProgress2", "(I)V" );
      (*env)->CallStaticVoidMethod( env, cls, methodID, progress );
      frame2->width = decctx->width;
      frame2->height = decctx->height;
      frame2->format = encctx->pix_fmt;
      frame3->format = encctx->pix_fmt;

      av_frame_get_buffer(frame2, 32);
      sws_scale(swsctx, (const uint8_t * const*)frame->data, frame->linesize, 0, frame2->height, frame2->data, frame2->linesize);
      av_frame_unref(frame);

      //rotate
      if (rotate == 0)
      {
        frame3->width = frame2->width;
        frame3->height = frame2->height;
        av_frame_get_buffer(frame3, 32);
        av_frame_copy(frame3, frame2);
      }
      else if (rotate == 90)
      {
        frame3->width = frame2->height;
        frame3->height = frame2->width;
        av_frame_get_buffer(frame3, 32);

        for ( ii = 0; ii < frame3->width; ii++)
          for ( jj = 0; jj < frame3->height; jj++) {
            *(frame3->data[0] + jj*frame3->width + ii) = *(frame2->data[0] + (frame2->height - 1 - ii)*frame2->width + jj);
          }

      }
      else if (rotate == 180)
      {
        frame3->width = frame2->width;
        frame3->height = frame2->height;
        av_frame_get_buffer(frame3, 32);


        for ( ii = 0; ii < frame3->width; ii++)
          for ( jj = 0; jj < frame3->height; jj++) {
            *(frame3->data[0] + jj*frame3->width + ii) = *(frame2->data[0] + (frame2->height - 1 - jj)*frame2->width + frame2->width - 1 - ii);
          }
      }
      else if (rotate == 270)
      {
        frame3->width = frame2->height;
        frame3->height = frame2->width;
        av_frame_get_buffer(frame3, 32);

        for ( ii = 0; ii < frame3->width; ii++)
          for ( jj = 0; jj < frame3->height; jj++) {
            *(frame3->data[0] + jj*frame3->width + ii) = *(frame2->data[0] + ii*frame2->width + frame2->width - 1 - jj);
          }
      }
      av_frame_unref(frame2);
      frame4->width = output_w;
      frame4->height = output_h;
      frame4->format = AV_PIX_FMT_BGR8;
      av_frame_get_buffer(frame4,32);
      sws_scale(swsctx2, (const uint8_t * const*)frame3->data, frame3->linesize, 0, frame3->height, frame4->data, frame4->linesize);
      av_frame_unref(frame3);
      pts_sum += ptsdelta;
      frame4->pts = (int)pts_sum;
      frame_num++;
      if (frame_num > frame_interval) {
        frame_num -= frame_interval;
        ret = avcodec_send_frame(encctx, frame4);
        av_frame_unref(frame4);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
          continue;
      }
      else {
        av_frame_unref(frame4);
      }
      while (ret >= 0)
      {
        ret = avcodec_receive_packet(encctx, &packet2);

        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
          continue;
        av_write_frame(outfmtctx, &packet2);

      }


    }
    av_packet_unref(&packet);
    av_packet_unref(&packet2);
  }
FLUSH:
  ret2 = 0;
  while (ret2 >= 0) {
    avcodec_send_frame(encctx, NULL);
    while (ret2 >= 0)
    {
      ret2 = avcodec_receive_packet(encctx, &packet2);
      if (ret2 == AVERROR(EAGAIN) || ret2 == AVERROR_EOF)
        continue;
      av_write_frame(outfmtctx, &packet2);
      av_packet_unref(&packet2);

    }
  }
  av_write_trailer(outfmtctx);
  avio_close( outfmtctx->pb );
  sws_freeContext( swsctx );
  sws_freeContext( swsctx2 );
  avcodec_free_context( &decctx );
  avcodec_free_context( &encctx );
  av_frame_free( &frame );
  av_frame_free( &frame2 );
  avformat_free_context( infmtctx );
  avformat_free_context( outfmtctx );
  free( input );
  free( output );
  return SUCCESSCODE;
FAIL:
  sws_freeContext( swsctx );
  sws_freeContext( swsctx2 );
  avcodec_free_context( &decctx );
  avcodec_free_context( &encctx );
  av_frame_free( &frame );
  av_frame_free( &frame2 );
  avformat_free_context( infmtctx );
  avformat_free_context( outfmtctx );
  free( input );
  free( output );
  return GIF2MP4_UNKNOWN_ERROR;
}
JNIEXPORT jintArray JNICALL Java_zz_app_gif2mp4_Utils_getMp4Size
  (JNIEnv *env, jclass cls, jstring path){

    char		*input		= jstringToChar( env, path );
    int ret[2];
    AVFormatContext * inputFmtCtx	= avformat_alloc_context();
    int err;
    err=avformat_open_input( &inputFmtCtx, input, NULL, NULL );
    if(err<0)return NULL;
    err=avformat_find_stream_info( inputFmtCtx, NULL );
    if(err<0)return NULL;
    int	vsnb	= -1;
    int	i	= 0, j = 0, k = 0;
    float	sum	= 0;
    int rotate=0;
    for (; i < inputFmtCtx->nb_streams; i++ )
    {
      if ( inputFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO )
      {
        vsnb = i;
        break;
      }
    }
    if ( vsnb != -1 )
    {
      AVDictionaryEntry *m = NULL;
      if((m = av_dict_get(inputFmtCtx->streams[vsnb]->metadata, "rotate", m, AV_DICT_IGNORE_SUFFIX)) != NULL) {
        rotate=atoi(m->value);
      }
      if(rotate==0||rotate==180)
      {
      ret[0]= inputFmtCtx->streams[vsnb]->codecpar->width;
      ret[1]=inputFmtCtx->streams[vsnb]->codecpar->height;
      }
      else if(rotate==90||rotate==270||rotate==-90 )
      {
      ret[0]= inputFmtCtx->streams[vsnb]->codecpar->height;
            ret[1]=inputFmtCtx->streams[vsnb]->codecpar->width;
      }
    }

    free( input );
    avformat_free_context( inputFmtCtx );
    jintArray arr=(*env)->NewIntArray(env,2);
    (*env)->SetIntArrayRegion(env,arr,0,2,ret);
    return arr;
  }