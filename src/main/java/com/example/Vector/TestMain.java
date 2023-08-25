package com.example.Vector;

public class TestMain {
  public static void main(String[] args) {
    TestObject testObject = TestObject.testObject;
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.println(testObject.cal());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    t.start();
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.println(testObject.cal());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    t1.start();
  }
}
