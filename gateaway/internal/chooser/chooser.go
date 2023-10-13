package chooser

import (
	"hash/fnv"
)

func ChooseStorage(data []byte, serverCount int) (int, error) {
	h := fnv.New32a()
	h.Write(data)
	sum := h.Sum32()
	return int(sum) % serverCount, nil
}
