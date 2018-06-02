// 
// FILE:  dht11_test1.pde
// PURPOSE: DHT11 library test sketch for Arduino
//

//Celsius to Fahrenheit conversion
double Fahrenheit(double celsius)
{
  return 1.8 * celsius + 32;
}

//Celsius to Kelvin conversion
double Kelvin(double celsius)
{
  return celsius + 273.15;
}

double dewPoint(double celsius, double humidity)
{
  // (1) Saturation Vapor Pressure = ESGG(T)
  double RATIO = 373.15 / (273.15 + celsius);
  double RHS = -7.90298 * (RATIO - 1);
  RHS += 5.02808 * log10(RATIO);
  RHS += -1.3816e-7 * (pow(10, (11.344 * (1 - 1/RATIO ))) - 1) ;
  RHS += 8.1328e-3 * (pow(10, (-3.49149 * (RATIO - 1))) - 1) ;
  RHS += log10(1013.246);

        // factor -3 is to adjust units - Vapor Pressure SVP * humidity
  double VP = pow(10, RHS - 3) * humidity;

        // (2) DEWPOINT = F(Vapor Pressure)
  double T = log(VP/0.61078);   // temp var
  return (241.88 * T) / (17.558 - T);
}

// delta max = 0.6544 wrt dewPoint()
// 6.9 x faster than dewPoint()
// reference: http://en.wikipedia.org/wiki/Dew_point
double dewPointFast(double celsius, double humidity)
{
  double a = 17.271;
  double b = 237.7;
  double temp = (a * celsius) / (b + celsius) + log(humidity*0.01);
  double Td = (b * temp) / (a - temp);
  return Td;
}

//-------------------------------------------------------------------------------ACTUAL CODE

#include <dht11.h>
#include<LiquidCrystal.h>
#define DHT11PIN 12
LiquidCrystal lcd(9,8,6,5,4,3);//rs,en,D4,D5,D6,D7

const char* ssid="CMRCET-IOT";
const char* password="12345678";
char res[130];
int led=13;

dht11 DHT11;

char check(char* ex,int timeout)
{
  int i=0;
  int j = 0,k=0;
                                            while (1)
                                            {
                                              sl:
                                                            if(Serial.available() > 0) //serial data present
                                                            {
                                                              res[i] = Serial.read();//reading into res[i]//0
                                                                            if(res[i] == 0x0a || res[i]=='>' || i == 100)//res[i]=enter//res[i]='>'//i=100
                                                                            {
                                                                              i++;
                                                                              res[i] = 0;break;//enters into loop when enter,> and i=100
                                                                            }
                                                              i++;
                                                            }
                                              j++;
                                              if(j == 30000)
                                              {
                                                k++;
                                            
                                                j = 0;  
                                              }
                                              if(k > timeout)
                                              {
                          
                                                return 1;
                                               }
                                            }
  if(!strncmp(ex,res,strlen(ex)))
  {
    return 0;
   }
  else
  {
    i=0;
    goto sl;
   }
}
void serialFlush(){
  while(Serial.available() > 0) {
    char t = Serial.read();
  }
} 

char buff[200];
 int sen1=0,sen2=0,sen3=0;
 int relay=13;
 
void setup() {

  char ret; 

  lcd.begin(16,2);
  lcd.print("Temps: ");
        lcd.setCursor(0,2);
  lcd.print("Hum% : " );

  pinMode(led,OUTPUT);
  digitalWrite(led,HIGH);
   
  Serial.begin(115200);
  Serial.println("DHT11 TEST PROGRAM ");
  Serial.print("LIBRARY VERSION: ");
  Serial.println(DHT11LIB_VERSION);
  Serial.println();
    
   Serial.begin(115200);
     
   delay(3000);
   serialFlush();
   
   st:
   Serial.println("ATE0"); // disable the echo of AT
   ret = check((char*)"OK",50);
   Serial.println("AT");
   ret = check((char*)"OK",50);
   
   if(ret != 0){
    delay(100);
    goto st;
   }
                                                                            
    Serial.println("AT+CWMODE = 1"); // client and access point
    ret  = check((char*)"OK",50);
    
    connectagain:
    serialFlush();
    Serial.print("AT+CWJAP=\""); // connect to a particular access point specified by ssid and pswd 
    Serial.print(ssid);
    Serial.print("\",\"");
    Serial.print(password);
    Serial.println("\"");
    
    if(check((char*)"OK",300)){
      goto connectagain;
    }
    Serial.println("AT+CIPMUX=1"); // 4 connections at a time to 4 diff channels; = 0 for single connection
}
int cl = 0;
//LOOP--------------------------------------------------------------------------------------------------------------LOOP
void loop() {
 cl++;

Serial.println("\n");

  int chk = DHT11.read(DHT11PIN);

  Serial.print("Read sensor: ");
  switch (chk)
  {
    case DHTLIB_OK: 
    Serial.println("OK"); 
    break;
    case DHTLIB_ERROR_CHECKSUM: 
    Serial.println("Checksum error"); 
    break;
    case DHTLIB_ERROR_TIMEOUT: 
    Serial.println("Time out error"); 
    break;
    default: 
    Serial.println("Unknown error"); 
    break;
  }

  Serial.print("Humidity (%): ");
  Serial.println((float)DHT11.humidity, 2);

  Serial.print("Temperature (°C): ");
  Serial.println((float)DHT11.temperature, 2);

  delay(2000);
 
  if(cl > 4)
  {
    cl = 0;
   serialFlush();
  Serial.println("AT+CIPSTART=4,\"TCP\",\"184.106.153.149\",80");
  if(!check((char*)"4,CONNECT",200))
  {
      serialFlush();
      Serial.println("AT+CIPSEND=4,76"); // 76 is the length of data 
      if(!check((char*)">",50))
      {
       
           digitalWrite(led,LOW);
           delay(1000);
           digitalWrite(led,HIGH);

            lcd.setCursor(7,0);
            lcd.print(DHT11.temperature);
            lcd.setCursor(9,0);
            lcd.print("C");
            lcd.setCursor(7,1);
            lcd.print(DHT11.humidity);
            lcd.setCursor(9,1);
            lcd.print("%");

          
          serialFlush();
       
          
         Serial.print("GET /update?api_key=UVCZEY477C68EJWV&");
          
            sprintf(buff,"field1=%02u",DHT11.temperature);
          Serial.print(buff);
           sprintf(buff,"&field2=%02u",DHT11.humidity);
          Serial.print(buff);
          
          //delay(1000);

         
          Serial.println("");//no choice
          if(!check((char*)"OK",200))
          {
              
              Serial.println("AT+CIPCLOSE=4");
          }
            
      }
     
    }
  }
  
}
