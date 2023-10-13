package main

import "github.com/Ziomidlo/Psr/internal/server"

func main() {
	s := server.New("localhost", "2137")
	s.Serve()
}
