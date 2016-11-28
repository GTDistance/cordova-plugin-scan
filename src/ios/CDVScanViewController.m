//
//  CDVScanViewController.m
//  cordova-plugin-scan
//
//  Created by ZhangJian on 16/8/3.
//  Copyright © 2016年 zhangjian. All rights reserved.
//

#import "CDVScanViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <QuartzCore/QuartzCore.h>

#define QRCodeWidth  260.0   //正方形二维码的边长
#define SCREENHeight  [UIScreen mainScreen].bounds.size.height
#define SCREENWidth  [UIScreen mainScreen].bounds.size.width
static const CGFloat kBorderW = 100;
static const CGFloat kMargin = 30;
@interface CDVScanViewController () <AVCaptureMetadataOutputObjectsDelegate> //用于处理采集信息的代理
// MARK: - Properties
@property (strong, nonatomic) AVCaptureSession* scanSession; //输入输出的中间桥梁
@property (strong, nonatomic) AVCaptureVideoPreviewLayer* scanLayer; //输入输出的中间桥梁
@end
@implementation CDVScanViewController

// MARK: - Lifecycle
- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"扫码充电";
    self.navigationItem.leftBarButtonItem=[[UIBarButtonItem alloc] initWithTitle:@"关闭" style:UIBarButtonItemStylePlain target:self action:@selector(handleClose:)];
    
    [self setupMaskView];
    
    [self setupScanWindowView];
    
    [self beginScanning];
}

- (void)viewWillAppear:(BOOL)animated
{
    if (!self.scanSession.isRunning) {
        [self.scanSession startRunning];
    }
}

- (void)viewDidDisappear:(BOOL)animated
{
    if (self.scanSession.isRunning) {
        [self.scanSession stopRunning];
    }
}

// MARK: - Private Methods
- (void)setupMaskView{
    
    //操作提示
    UILabel * tipLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, SCREENHeight*0.9-kBorderW*2, SCREENWidth, kBorderW)];
    tipLabel.text = @"请扫描充电桩上的二维码";
    tipLabel.textColor = [UIColor whiteColor];
    tipLabel.textAlignment = NSTextAlignmentCenter;
    tipLabel.lineBreakMode = NSLineBreakByWordWrapping;
    tipLabel.numberOfLines = 0;
    tipLabel.font = [UIFont systemFontOfSize:15];
    tipLabel.backgroundColor = [UIColor clearColor];
    [self.view addSubview:tipLabel];
    
    //设置统一的视图颜色和视图的透明度
    UIColor *color = [UIColor blackColor];
    float alpha = 0.3;
    
    //设置扫描区域外部上部的视图
    UIView *topView = [[UIView alloc]init];
    topView.frame = CGRectMake(0, 0, SCREENWidth, (SCREENHeight-QRCodeWidth)/2.0-64);
    topView.backgroundColor = color;
    topView.alpha = alpha;
    
    //设置扫描区域外部左边的视图
    UIView *leftView = [[UIView alloc]init];
    leftView.frame = CGRectMake(0, topView.frame.size.height, (SCREENWidth-QRCodeWidth)/2.0,QRCodeWidth);
    leftView.backgroundColor = color;
    leftView.alpha = alpha;
    
    //设置扫描区域外部右边的视图
    UIView *rightView = [[UIView alloc]init];
    rightView.frame = CGRectMake((SCREENWidth-QRCodeWidth)/2.0+QRCodeWidth,topView.frame.size.height, (SCREENWidth-QRCodeWidth)/2.0,QRCodeWidth);
    rightView.backgroundColor = color;
    rightView.alpha = alpha;
    
    //设置扫描区域外部底部的视图
    UIView *botView = [[UIView alloc]init];
    botView.frame = CGRectMake(0, QRCodeWidth+topView.frame.size.height,SCREENWidth,SCREENHeight-QRCodeWidth-topView.frame.size.height);
    botView.backgroundColor = color;
    botView.alpha = alpha;
    
    //将设置好的扫描二维码区域之外的视图添加到视图图层上
    [self.view addSubview:topView];
    [self.view addSubview:leftView];
    [self.view addSubview:rightView];
    [self.view addSubview:botView];
    
    //闪光灯
    UIButton * flashBtn=[UIButton buttonWithType:UIButtonTypeCustom];
    flashBtn.frame = CGRectMake((SCREENWidth-60)/2,SCREENHeight*0.9-kBorderW*2+80, 60, 60);
    [flashBtn setBackgroundImage:[UIImage imageNamed:@"sgd"] forState:UIControlStateNormal];
    flashBtn.contentMode=UIViewContentModeScaleAspectFit;
    [flashBtn addTarget:self action:@selector(openFlash:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:flashBtn];
}

- (void)setupScanWindowView{
    //设置扫描区域的位置
    UIView *scanWindow = [[UIView alloc] initWithFrame:CGRectMake((SCREENWidth-QRCodeWidth)/2.0,(SCREENHeight-QRCodeWidth)/2.0-64,QRCodeWidth,QRCodeWidth)];
    scanWindow.clipsToBounds = YES;
    [self.view addSubview:scanWindow];
    
    //设置扫描区域的动画效果
    CGFloat scanNetImageViewH = 2;
    CGFloat scanNetImageViewW = scanWindow.frame.size.width;
    UIImageView *scanNetImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"scanLine"]];
    scanNetImageView.frame = CGRectMake(0, -scanNetImageViewH, scanNetImageViewW, scanNetImageViewH);
    CABasicAnimation *scanNetAnimation = [CABasicAnimation animation];
    scanNetAnimation.keyPath =@"transform.translation.y";
    scanNetAnimation.byValue = @(QRCodeWidth);
    scanNetAnimation.duration = 2.0;
    scanNetAnimation.repeatCount = MAXFLOAT;
    [scanNetImageView.layer addAnimation:scanNetAnimation forKey:nil];
    [scanWindow addSubview:scanNetImageView];
    
    //设置扫描区域的四个角的边框
    CGFloat buttonWH = 18;
    UIButton *topLeft = [[UIButton alloc]initWithFrame:CGRectMake(0,0, buttonWH, buttonWH)];
    [topLeft setImage:[UIImage imageNamed:@"left-up"]forState:UIControlStateNormal];
    [scanWindow addSubview:topLeft];
    
    UIButton *topRight = [[UIButton alloc]initWithFrame:CGRectMake(QRCodeWidth - buttonWH,0, buttonWH, buttonWH)];
    [topRight setImage:[UIImage imageNamed:@"right-up"]forState:UIControlStateNormal];
    [scanWindow addSubview:topRight];
    
    UIButton *bottomLeft = [[UIButton alloc]initWithFrame:CGRectMake(0,QRCodeWidth - buttonWH, buttonWH, buttonWH)];
    [bottomLeft setImage:[UIImage imageNamed:@"left-down"]forState:UIControlStateNormal];
    [scanWindow addSubview:bottomLeft];
    
    UIButton *bottomRight = [[UIButton alloc]initWithFrame:CGRectMake(QRCodeWidth-buttonWH,QRCodeWidth-buttonWH, buttonWH, buttonWH)];
    [bottomRight setImage:[UIImage imageNamed:@"right-down"]forState:UIControlStateNormal];
    [scanWindow addSubview:bottomRight];
}

- (void)beginScanning{
    // Do any additional setup after loading the view, typically from a nib.
    //获取摄像设备
    AVCaptureDevice* device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    //创建输入流
    AVCaptureDeviceInput* input = [AVCaptureDeviceInput deviceInputWithDevice:device error:nil];
    if (!input) return;
    //创建输出流
    AVCaptureMetadataOutput* output = [[AVCaptureMetadataOutput alloc] init];
    //设置代理 在主线程里刷新
    [output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    
    //初始化链接对象
    self.scanSession = [[AVCaptureSession alloc] init];
    //高质量采集率
    [self.scanSession setSessionPreset:AVCaptureSessionPresetHigh];
    
    [self.scanSession addInput:input];
    [self.scanSession addOutput:output];
    //设置扫码支持的编码格式(如下设置条形码和二维码兼容)
    output.metadataObjectTypes = @[ AVMetadataObjectTypeQRCode, AVMetadataObjectTypeEAN13Code, AVMetadataObjectTypeEAN8Code, AVMetadataObjectTypeCode128Code, AVMetadataObjectTypeCode93Code ];
    //设置扫描有效范围
    //特别注意的地方：有效的扫描区域，定位是以设置的右顶点为原点。屏幕宽所在的那条线为y轴，屏幕高所在的线为x轴
    CGFloat x = ((SCREENHeight-QRCodeWidth)/2.0)/SCREENHeight;
    CGFloat y = ((SCREENWidth-QRCodeWidth)/2.0)/SCREENWidth;
    CGFloat width = QRCodeWidth/SCREENHeight;
    CGFloat height = QRCodeWidth/SCREENWidth;
    output.rectOfInterest = CGRectMake(x, y, width, height);
    //    [output setRectOfInterest:CGRectMake(124/SCREENHeight, ((SCREENWidth-220)/2)/SCREENWidth, 220/SCREENHeight, 220/SCREENWidth)];
    
    self.scanLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.scanSession];
    self.scanLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    [self.view.layer insertSublayer:self.scanLayer atIndex:0];
    //开始捕获
    [self.scanSession startRunning];
}

-(void)handleClose:(id)sender{
    [self.navigationController dismissViewControllerAnimated:YES completion:NULL];
}

- (void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    self.scanLayer.frame = self.view.layer.bounds;
}


- (void)captureOutput:(AVCaptureOutput*)captureOutput didOutputMetadataObjects:(NSArray*)metadataObjects fromConnection:(AVCaptureConnection*)connection
{
    [self.scanSession stopRunning];
    if (metadataObjects.count > 0) {
        //[session stopRunning];
        AVMetadataMachineReadableCodeObject* metadataObject = [metadataObjects objectAtIndex:0];
        //输出扫描字符串
        NSString *str = metadataObject.stringValue;
        NSLog(@"%@",str);
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:@"scan" object:self userInfo:@{@"content":str}];
            [self handleClose:NULL];
        });
        
        
    }
}

//MARK:-  闪光灯
-(void)openFlash:(UIButton*)button{
    NSLog(@"闪光灯");
    button.selected = !button.selected;
    if (button.selected) {
        [self turnTorchOn:YES];
    }
    else{
        [self turnTorchOn:NO];
    }
}

//MARK:- 开关闪光灯
- (void)turnTorchOn:(BOOL)on{
    Class captureDeviceClass = NSClassFromString(@"AVCaptureDevice");
    if (captureDeviceClass != nil) {
        AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        if ([device hasTorch] && [device hasFlash]){
            [device lockForConfiguration:nil];
            if (on) {
                [device setTorchMode:AVCaptureTorchModeOn];
                [device setFlashMode:AVCaptureFlashModeOn];
                
            } else {
                [device setTorchMode:AVCaptureTorchModeOff];
                [device setFlashMode:AVCaptureFlashModeOff];
            }
            [device unlockForConfiguration];
        }
    }
}

@end