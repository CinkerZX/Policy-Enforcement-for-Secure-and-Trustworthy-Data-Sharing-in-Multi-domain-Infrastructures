//Import the policy
{ include("policy.asl") }
//Perceive the environment 
+normal_condition:true <-
	!perceive_environment.
+~normal_condition:true <-
	!perceive_environment.
//Update it's belief about environment
+!perceive_environment : ~normal_condition <-
	.print("Now is under emergency condition");
	!judge.
+!perceive_environment : normal_condition <-
	.print("Now is under normal condition");
	!judge.
//If there is request, judge and then send signature
+!judge: need_aut(Dataset,Sender,Recipient,Purpose) & signature(Dataset,Sender,Recipient,Purpose)<-
	.print("This request is compliant.");
	!signature;
	signature; //give signature
	+signed(Dataset,Sender,Recipient,Purpose); // log: which request has been signed
	-need_aut(Dataset,Sender,Recipient,Purpose). // delete the request in belief

//If there is request need to be audit and the request be rejected, give rejection
+!judge: need_aut(Dataset,Sender,Recipient,Purpose)<-
	.print("This request is non-compliant."); 
	!rejection;
	rejection; //give rejection
	-need_aut(Dataset,Sender,Recipient,Purpose).// delete the request in belief
	
//If there is no pending request
+!judge <- 
	.print("No pending request.");
	waiting.
+!signature
<- .print("Give the signature.").
+!rejection
<- .print("Give the rejection.").

