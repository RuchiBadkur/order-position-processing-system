package com.ruchi.order_update_service.throttle;

public class RateLimiter {

    private final long intervalNanos;
    private long nextAllowedTime;

    public RateLimiter(int maxEventsPerSecond){
        if(maxEventsPerSecond <= 0){
            throw new IllegalArgumentException(
                    "max-events-per-second must be greater than 0"
            );
        }

        this.intervalNanos = 1_000_000_000L / maxEventsPerSecond;
        this.nextAllowedTime = System.nanoTime();
    }

    public void acquire(){
        long now = System.nanoTime();

        if (now < nextAllowedTime){
            long sleepNanos = nextAllowedTime - now;

            try {
                long milis = sleepNanos / 1_000_000L;
                int nanos = (int)(sleepNanos % 1_000_000L);

                Thread.sleep(milis, nanos);
            }catch(InterruptedException e ){
                Thread.currentThread().interrupt();

                throw new RuntimeException(
                        "Rate limiter interrupted",
                        e
                );
            }
        }
        nextAllowedTime = Math.max(nextAllowedTime, System.nanoTime())
                + intervalNanos;
    }

}
