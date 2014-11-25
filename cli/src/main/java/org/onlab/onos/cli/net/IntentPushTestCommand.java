/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onlab.onos.cli.net;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.onos.cli.AbstractShellCommand;
import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;
import org.onlab.onos.net.flow.DefaultTrafficSelector;
import org.onlab.onos.net.flow.DefaultTrafficTreatment;
import org.onlab.onos.net.flow.TrafficSelector;
import org.onlab.onos.net.flow.TrafficTreatment;
import org.onlab.onos.net.intent.Intent;
import org.onlab.onos.net.intent.IntentEvent;
import org.onlab.onos.net.intent.IntentEvent.Type;
import org.onlab.onos.net.intent.IntentListener;
import org.onlab.onos.net.intent.IntentOperations;
import org.onlab.onos.net.intent.IntentService;
import org.onlab.onos.net.intent.PointToPointIntent;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.onlab.onos.net.DeviceId.deviceId;
import static org.onlab.onos.net.PortNumber.portNumber;

/**
 * Installs point-to-point connectivity intents.
 */
@Command(scope = "onos", name = "push-test-intents",
         description = "Installs random intents to test throughput")
public class IntentPushTestCommand extends AbstractShellCommand
        implements IntentListener {

    @Argument(index = 0, name = "ingressDevice",
              description = "Ingress Device/Port Description",
              required = true, multiValued = false)
    String ingressDeviceString = null;

    @Argument(index = 1, name = "egressDevice",
              description = "Egress Device/Port Description",
              required = true, multiValued = false)
    String egressDeviceString = null;

    @Argument(index = 2, name = "count",
              description = "Number of intents to push",
              required = true, multiValued = false)
    String countString = null;

    private IntentService service;
    private CountDownLatch latch;
    private long start, end;
    private int count;
    private boolean add;

    @Override
    protected void execute() {
        service = get(IntentService.class);


        DeviceId ingressDeviceId = deviceId(getDeviceId(ingressDeviceString));
        PortNumber ingressPortNumber = portNumber(getPortNumber(ingressDeviceString));
        ConnectPoint ingress = new ConnectPoint(ingressDeviceId, ingressPortNumber);

        DeviceId egressDeviceId = deviceId(getDeviceId(egressDeviceString));
        PortNumber egressPortNumber = portNumber(getPortNumber(egressDeviceString));
        ConnectPoint egress = new ConnectPoint(egressDeviceId, egressPortNumber);

        count = Integer.parseInt(countString);

        service.addListener(this);

        add = true;
        latch = new CountDownLatch(count);
        IntentOperations operations = generateIntents(ingress, egress);
        submitIntents(operations);

        add = false;
        latch = new CountDownLatch(count);
        operations = generateIntents(ingress, egress);
        submitIntents(operations);

        service.removeListener(this);
    }

    private IntentOperations generateIntents(ConnectPoint ingress, ConnectPoint egress) {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4);
        TrafficTreatment treatment = DefaultTrafficTreatment.builder().build();

        IntentOperations.Builder ops = IntentOperations.builder();
        for (int i = 1; i <= count; i++) {
            TrafficSelector s = selector
                    .matchEthSrc(MacAddress.valueOf(i))
                    .build();
            Intent intent = new PointToPointIntent(appId(), s, treatment,
                                                   ingress, egress);
            if (add) {
                ops.addSubmitOperation(intent);
            } else {
                ops.addWithdrawOperation(intent.id());
            }
        }
        return ops.build();
    }

    private void submitIntents(IntentOperations ops) {
        start = System.currentTimeMillis();
        service.execute(ops);
        try {
            if (latch.await(10, TimeUnit.SECONDS)) {
                printResults(count);
            } else {
                print("Failure: %d intents not installed", latch.getCount());
            }
        } catch (InterruptedException e) {
            print(e.toString());
        }
    }

    private void printResults(int count) {
        long delta = end - start;
        String text = add ? "install" : "withdraw";
        print("Time to %s %d intents: %d ms", text, count, delta);
    }

    /**
     * Extracts the port number portion of the ConnectPoint.
     *
     * @param deviceString string representing the device/port
     * @return port number as a string, empty string if the port is not found
     */
    private String getPortNumber(String deviceString) {
        int slash = deviceString.indexOf('/');
        if (slash <= 0) {
            return "";
        }
        return deviceString.substring(slash + 1, deviceString.length());
    }

    /**
     * Extracts the device ID portion of the ConnectPoint.
     *
     * @param deviceString string representing the device/port
     * @return device ID string
     */
    private String getDeviceId(String deviceString) {
        int slash = deviceString.indexOf('/');
        if (slash <= 0) {
            return "";
        }
        return deviceString.substring(0, slash);
    }

    @Override
    public void event(IntentEvent event) {
        Type expected = add ? Type.INSTALLED : Type.WITHDRAWN;
        if (event.type() == expected) {
            end = event.time();
            if (latch != null) {
                latch.countDown();
            } else {
                log.warn("install event latch is null");
            }
        } else if (event.type() != Type.SUBMITTED) {
            log.info("Unexpected intent event: {}", event);
        }
    }
}
