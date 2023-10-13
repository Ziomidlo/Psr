package server

import (
	"net"

	"github.com/Ziomidlo/Psr/internal/chooser"
)

type server struct {
	host string
	port string
}

func New(host, port string) *server {
	return &server{
		host: host,
		port: port,
	}
}

func (s *server) Serve() error {
	listen, err := net.Listen("tcp", s.host+":"+s.port)
	if err != nil {
		return err
	}
	defer listen.Close()
	for {
		conn, err := listen.Accept()
		if err != nil {
			return err
		}
		go handleRequest(conn)
	}
}

func handleRequest(conn net.Conn) error {
	buffer := make([]byte, 1024)
	_, err := conn.Read(buffer)
	if err != nil {
		return err
	}
	chooser.ChooseStorage(buffer, 10)
	return nil
}
