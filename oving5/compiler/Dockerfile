FROM gcc:9.3.0 
RUN mkdir /compile
WORKDIR /compile
COPY main.cpp .
RUN g++ -o main main.cpp
CMD ["./main"]