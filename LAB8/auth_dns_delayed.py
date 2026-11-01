#!/usr/bin/env python3
import argparse, socket, struct, time, ipaddress

ALLOWED_BIND = ipaddress.ip_address("172.16.6.20")
TARGET = "www.external.lab9.test."
REAL_IP = "172.16.6.10"

def parse_qname(data, off=12):
    labels=[]
    while True:
        ln=data[off]; off+=1
        if ln==0: break
        labels.append(data[off:off+ln].decode('ascii',errors='ignore')); off+=ln
    return '.'.join(labels)+'.', off

def build_response(q, real_ip, delay):
    tid=q[:2]
    flags_q=struct.unpack('!H',q[2:4])[0]
    qname,end=parse_qname(q,12)
    question=q[12:end+4]
    rd=flags_q & 0x0100
    if qname.lower()==TARGET:
        flags=0x8400 | rd  # QR + AA + copy RD
        header=tid+struct.pack('!HHHHH',flags,1,1,0,0)
        rdata=socket.inet_aton(real_ip)
        answer=b'\xc0\x0c'+struct.pack('!HHIH',1,1,60,4)+rdata
        time.sleep(delay)
        return header+question+answer,qname
    flags=0x8403 | rd
    header=tid+struct.pack('!HHHHH',flags,1,0,0,0)
    time.sleep(delay)
    return header+question,qname

def main():
    ap=argparse.ArgumentParser(description='LAB9 delayed authoritative DNS simulator')
    ap.add_argument('--bind',default='172.16.6.20')
    ap.add_argument('--delay',type=float,default=1.5)
    args=ap.parse_args()
    if ipaddress.ip_address(args.bind)!=ALLOWED_BIND:
        raise SystemExit('LAB safety: bind address must be 172.16.6.20')
    s=socket.socket(socket.AF_INET,socket.SOCK_DGRAM); s.bind((args.bind,53))
    print(f'LAB9 AUTH-DNS listening {args.bind}:53, delay={args.delay}s, target={TARGET} -> {REAL_IP}')
    while True:
        q,addr=s.recvfrom(4096)
        try:
            resp,name=build_response(q,REAL_IP,args.delay)
            print(f'query {name} from {addr[0]}:{addr[1]}')
            s.sendto(resp,addr)
        except Exception as e:
            print('parse error:',e)

if __name__=='__main__': main()
