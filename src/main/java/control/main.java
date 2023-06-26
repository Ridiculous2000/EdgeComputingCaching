package control;

import BaseLine.BaseLRU;
import BaseLine.BaseRandom;
import BaseLine.BaseUCO;
import our_algorithm.OurAlgorithm;

import java.io.IOException;

public class main {

    public static int minTimestamp = 0;
    public static int maxTimestamp = 50;
    public static void main(String[] args) throws IOException {
        //导入数据集基本数据
//        InitBasicData initBasicData = new InitBasicData();
//        initBasicData.insertBasicUser();
//        initBasicData.insertBasicEdgeServer();
        //生成初始化数据
//        GenerateData generateData = new GenerateData();
//        generateData.addExperimentalUser(400);
//        generateData.addExperimentalServer(40);
//        generateData.newPopularData(100,3);
//        generateData.newUserDataProbability(1.5);
//        ArrayList<Integer> timestampList = new ArrayList<Integer>();
//        for(int i=1;i<=80;i++){
//            timestampList.add(i);
//        }
//        generateData.newRequest(timestampList);
        //构建请求矩阵
//        OtherUtils.buildRequestMatrix(0,50);
        //矩阵分解是python实现
        //本文算法
//        OurAlgorithm ourAlgorithm = new OurAlgorithm();
//        //初始化算法所需内容
//        ourAlgorithm.initializeData();
//        ourAlgorithm.findBestDecision(51);
//        System.out.println("adw");
        BaseRandom baseRandom=new BaseRandom();
        baseRandom.initializeData(51,80);
        baseRandom.experiment(51,80);
        //Random
        /*
Timestamp51 SumQoE: 272.19600545192014 FIndex: 0.6656944107772368FinalValue: 0.6755581460122791
Timestamp52 SumQoE: 304.0640892315899 FIndex: 0.6713853742768314FinalValue: 0.7305686068115936
Timestamp53 SumQoE: 314.05408523390025 FIndex: 0.6947295466070421FinalValue: 0.7549999909255144
Timestamp54 SumQoE: 327.218858744364 FIndex: 0.7122453231033294FinalValue: 0.7827798722750497
Timestamp55 SumQoE: 328.3215439669034 FIndex: 0.7084738079696735FinalValue: 0.7833605092680634
Timestamp56 SumQoE: 332.15614587339155 FIndex: 0.7157391504669169FinalValue: 0.7921732932779583
Timestamp57 SumQoE: 335.49875785740744 FIndex: 0.7397408617340979FinalValue: 0.8057448836737118
Timestamp58 SumQoE: 339.5174377368947 FIndex: 0.7520013575485451FinalValue: 0.8165295154110063
Timestamp59 SumQoE: 342.7904963845154 FIndex: 0.7552254296632499FinalValue: 0.8230593038619421
Timestamp60 SumQoE: 339.1331552133735 FIndex: 0.7446361579112544FinalValue: 0.8134339779927074
Timestamp61 SumQoE: 341.544731136566 FIndex: 0.7487972340151389FinalValue: 0.8188402965659897
Timestamp62 SumQoE: 338.6974747217349 FIndex: 0.7344967636739184FinalValue: 0.8093280457608643
Timestamp63 SumQoE: 345.3575261996225 FIndex: 0.7600368597510259FinalValue: 0.8289414969163795
Timestamp64 SumQoE: 344.8102967197723 FIndex: 0.7554826692555043FinalValue: 0.8265113842847885
Timestamp65 SumQoE: 345.8830668543107 FIndex: 0.75482705277508FinalValue: 0.8280807956822112
Timestamp66 SumQoE: 345.7408495637714 FIndex: 0.7621608064239008FinalValue: 0.8302883514142526
Timestamp67 SumQoE: 349.94952757287757 FIndex: 0.772611632641287FinalValue: 0.8407864235018917
Timestamp68 SumQoE: 350.236731968577 FIndex: 0.7828317655839353FinalValue: 0.8446718084756067
Timestamp69 SumQoE: 350.28402834860583 FIndex: 0.7789479177883287FinalValue: 0.8434560198437859
Timestamp70 SumQoE: 350.3096172722832 FIndex: 0.7809545738756978FinalValue: 0.8441675534123713
Timestamp71 SumQoE: 351.01535431490197 FIndex: 0.7881543661666636FinalValue: 0.8477437125803912
Timestamp72 SumQoE: 351.5257982537116 FIndex: 0.7838102664594775FinalValue: 0.8471464192426786
Timestamp73 SumQoE: 352.66832970447496 FIndex: 0.7922805190975952FinalValue: 0.8518740558733233
Timestamp74 SumQoE: 350.60471982732116 FIndex: 0.7789841624714231FinalValue: 0.8440025872026764
Timestamp75 SumQoE: 352.89248659106624 FIndex: 0.7949374389329916FinalValue: 0.853133290629441
Timestamp76 SumQoE: 350.8245625931183 FIndex: 0.7793976685405832FinalValue: 0.8445068271687249
Timestamp77 SumQoE: 351.4813956102588 FIndex: 0.7876831789455401FinalValue: 0.8483633856656114
Timestamp78 SumQoE: 348.8137429679915 FIndex: 0.7696813906435865FinalValue: 0.837916701827848
Timestamp79 SumQoE: 353.3507353754484 FIndex: 0.7998329496728028FinalValue: 0.8555288755166816
Timestamp80 SumQoE: 353.4881249093791 FIndex: 0.7966241139061783FinalValue: 0.8546882461510247
         */
//        BaseUCO baseUCO=new BaseUCO();
//        baseUCO.initializeData(51,80);
//        baseUCO.experiment(51,80);
        //UCO
        /*
Timestamp51 SumQoE: 263.32667800028673 FIndex: 0.673325212977485FinalValue: 0.6633195343263063
Timestamp52 SumQoE: 269.2929840235282 FIndex: 0.6739628456129335FinalValue: 0.6734759219101916
Timestamp53 SumQoE: 272.00365847189374 FIndex: 0.6682223566128721FinalValue: 0.6760802163241136
Timestamp54 SumQoE: 271.28259482035423 FIndex: 0.668210041902745FinalValue: 0.674874338668172
Timestamp55 SumQoE: 263.9316489452994 FIndex: 0.670493937991602FinalValue: 0.663384060906033
Timestamp56 SumQoE: 268.6605932529824 FIndex: 0.6743274967738022FinalValue: 0.6725434876795714
Timestamp57 SumQoE: 266.9475814387028 FIndex: 0.666193867577058FinalValue: 0.6669772582568573
Timestamp58 SumQoE: 268.84496497137593 FIndex: 0.6727082549692052FinalValue: 0.6723110266086949
Timestamp59 SumQoE: 270.94258580815506 FIndex: 0.6653770446596876FinalValue: 0.673363324566821
Timestamp60 SumQoE: 261.5050534610836 FIndex: 0.6743346095917877FinalValue: 0.6606199589657352
Timestamp61 SumQoE: 264.7205185399153 FIndex: 0.6667337221144439FinalValue: 0.6634454382713401
Timestamp62 SumQoE: 264.2289911225233 FIndex: 0.6715600670128246FinalValue: 0.6642350075418137
Timestamp63 SumQoE: 268.86756710617163 FIndex: 0.67297746876432FinalValue: 0.6724384347650595
Timestamp64 SumQoE: 266.444923669333 FIndex: 0.6695518561144458FinalValue: 0.6672588248203702
Timestamp65 SumQoE: 264.9691315562071 FIndex: 0.668034374615854FinalValue: 0.6642933441322966
Timestamp66 SumQoE: 262.5689890065399 FIndex: 0.6726228562199632FinalValue: 0.6618226004175541
Timestamp67 SumQoE: 269.24091722932064 FIndex: 0.6718359344321927FinalValue: 0.6726801735262654
Timestamp68 SumQoE: 268.225927857175 FIndex: 0.6644330365209297FinalValue: 0.6685208919356015
Timestamp69 SumQoE: 264.3898813777622 FIndex: 0.6736119874624629FinalValue: 0.6651871314504246
Timestamp70 SumQoE: 269.3660600459912 FIndex: 0.6679805554619589FinalValue: 0.6716036185639718
Timestamp71 SumQoE: 269.04061934915245 FIndex: 0.673293196523315FinalValue: 0.6728320977563591
Timestamp72 SumQoE: 268.0779174746137 FIndex: 0.6710280463381075FinalValue: 0.670472544570392
Timestamp73 SumQoE: 271.9319561573995 FIndex: 0.6584983008746462FinalValue: 0.6727193605538813
Timestamp74 SumQoE: 268.26041656929505 FIndex: 0.6609855810746541FinalValue: 0.6674292213070432
Timestamp75 SumQoE: 270.80178365458437 FIndex: 0.6645218529801995FinalValue: 0.672843590417707
Timestamp76 SumQoE: 268.089446480506 FIndex: 0.6713843689107051FinalValue: 0.6706105337710784
Timestamp77 SumQoE: 266.3726456301457 FIndex: 0.6707942616524847FinalValue: 0.6675524966010711
Timestamp78 SumQoE: 265.1918177200253 FIndex: 0.6742045702659631FinalValue: 0.6667212196220298
Timestamp79 SumQoE: 266.1338470015516 FIndex: 0.6815182802144686FinalValue: 0.6707291717407422
Timestamp80 SumQoE: 267.3321553757088 FIndex: 0.6698035439361849FinalValue: 0.6688214402715763
         */
        //UCO:鏈�缁堢粨鏋� SumQoE: 263.32667800028673 鈥斺�� FIndex: 0.673325212977485 鈥斺�� FinalValue: 0.6633195343263063
//        BaseLRU baseLRU=new BaseLRU();
//        baseLRU.initializeData(51,80);
//        baseLRU.experiment(51,80);
        //GCO
        /*
        Timestamp51 SumQoE: 271.14537615465946 FIndex: 0.6711272408543065FinalValue: 0.6756180405425347
        Timestamp52 SumQoE: 275.77182331036806 FIndex: 0.6653146999119979FinalValue: 0.6813912721546127
        Timestamp53 SumQoE: 276.81361207424743 FIndex: 0.6678989468732577FinalValue: 0.6839890024148317
        Timestamp54 SumQoE: 276.38306868117274 FIndex: 0.6603031619255377FinalValue: 0.6807395017771339
        Timestamp55 SumQoE: 271.90893766708626 FIndex: 0.6633449263617386FinalValue: 0.6742965382323899
        Timestamp56 SumQoE: 273.2487646845993 FIndex: 0.6595769359067173FinalValue: 0.6752735864432379
        Timestamp57 SumQoE: 275.07426616680215 FIndex: 0.6627120760092029FinalValue: 0.6793611356144046
        Timestamp58 SumQoE: 273.17373196174617 FIndex: 0.667630606432695FinalValue: 0.6778330887471419
        Timestamp59 SumQoE: 278.6996175198675 FIndex: 0.6547581470025579FinalValue: 0.6827520782006319
        Timestamp60 SumQoE: 269.8997697532785 FIndex: 0.6697762690768676FinalValue: 0.6730917059477534
        Timestamp61 SumQoE: 276.0627758561125 FIndex: 0.664088645179937FinalValue: 0.6814675081534999
        Timestamp62 SumQoE: 272.002142482885 FIndex: 0.6610970050632702FinalValue: 0.673702572492565
        Timestamp63 SumQoE: 278.0341480624221 FIndex: 0.6694949694803107FinalValue: 0.6865552365974738
        Timestamp64 SumQoE: 272.0177656257237 FIndex: 0.6639405228867177FinalValue: 0.6746764503384455
        Timestamp65 SumQoE: 275.4896398650568 FIndex: 0.6630027821080775FinalValue: 0.6801503271444539
        Timestamp66 SumQoE: 268.1547557583649 FIndex: 0.6698364855479927FinalValue: 0.6702034214466058
        Timestamp67 SumQoE: 275.1957307601336 FIndex: 0.6714840402419795FinalValue: 0.6824875646808826
        Timestamp68 SumQoE: 277.6794910745408 FIndex: 0.6677217473027126FinalValue: 0.6853730675584723
        Timestamp69 SumQoE: 275.94494139650936 FIndex: 0.6681221520763044FinalValue: 0.6826156196862838
        Timestamp70 SumQoE: 278.41339190113484 FIndex: 0.6648229711238896FinalValue: 0.6856299768765212
        Timestamp71 SumQoE: 273.76288571090095 FIndex: 0.6664013721311095FinalValue: 0.6784052668952048
        Timestamp72 SumQoE: 277.2841277971968 FIndex: 0.6592277918871243FinalValue: 0.681882810291036
        Timestamp73 SumQoE: 274.8625491776207 FIndex: 0.6620282308677954FinalValue: 0.6787803255852997
        Timestamp74 SumQoE: 272.3964839526113 FIndex: 0.6631858582782028FinalValue: 0.6750560926804198
        Timestamp75 SumQoE: 276.9649246904339 FIndex: 0.6647652444737273FinalValue: 0.6831966226419656
        Timestamp76 SumQoE: 274.0314646715295 FIndex: 0.6673726969158107FinalValue: 0.679176673424486
        Timestamp77 SumQoE: 271.170666391043 FIndex: 0.6666111312740015FinalValue: 0.6741548210764056
        Timestamp78 SumQoE: 268.65856670790976 FIndex: 0.6734440087720093FinalValue: 0.6722456141038528
        Timestamp79 SumQoE: 270.4164752483636 FIndex: 0.674496514644847FinalValue: 0.6755262969622217
        Timestamp80 SumQoE: 275.02287673872456 FIndex: 0.6715482795007477FinalValue: 0.6822208877314568
         */
    }
}
