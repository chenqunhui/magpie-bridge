namespace java com.cqh.magpie.rpc.thrift

service EchoService{
	string echo(
		1:string str
	)
	
	string pt(
	    1:string str
	)
}