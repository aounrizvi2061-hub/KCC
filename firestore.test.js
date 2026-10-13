const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_kcc";
const BOB_UID = "bob_kcc";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// Unauthenticated tests
test("Unauthenticated user: cannot read teams", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("teams").get());
});

test("Unauthenticated user: cannot create match", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("matches").add({
      userId: "some_user",
      title: "Test Match",
      format: "LIMITED_OVERS",
      teamAId: "t1",
      teamBId: "t2",
      status: "LIVE"
    })
  );
});

// Authenticated tests
test("Authenticated user: can create valid team", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("teams").doc("team_1").set({
      userId: ALICE_UID,
      name: "KCC Knights",
      shortName: "KCC"
    })
  );
});

test("Cross-user isolation: Bob cannot update Alice's team", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.collection("teams").doc("team_1").set({
    userId: ALICE_UID,
    name: "KCC Knights",
    shortName: "KCC"
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("teams").doc("team_1").update({
      name: "Hacked Team"
    })
  );
});

test("Authenticated user: can create match and deliveries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").set({
      userId: ALICE_UID,
      title: "KCC Championship Final",
      format: "LIMITED_OVERS",
      teamAId: "team_1",
      teamBId: "team_2",
      status: "LIVE"
    })
  );

  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").collection("deliveries").doc("del_1").set({
      userId: ALICE_UID,
      matchId: "match_1",
      inningsNumber: 1,
      strikerId: "p1",
      bowlerId: "p2",
      runsBat: 4,
      isWicket: false
    })
  );
});
