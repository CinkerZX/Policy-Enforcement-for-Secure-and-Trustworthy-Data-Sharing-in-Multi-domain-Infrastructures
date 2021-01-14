//policy for data "parking1"
policy1(parking1, omc, vmca, traffic_diversion, emergency).
//Conditions for giving signature
signature(Dataset,Sender,Recipient,Purpose) :- ~normal_condition & policy1(D,S,R,Pur,Env) & D == Dataset & S == Sender & R == Recipient & Pur == Purpose.

rejection(Dataset,Sender,Recipient,Purpose) :- policy(D,S,R,Pur) & (D \== Dataset | S \== Sender | R \== Recipient | Pur \== Purpose).
